package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * LinkedTransferQueue是LinkedBlockingQueue、SynchronousQueue（公平模式）、ConcurrentLinkedQueue三者的集合体，
 * 它综合了这三者的方法，并且提供了更加高效的实现方式
 * 无界队列，内部实现 LockSupport + CAS
 *
 * LinkedTransferQueue与SynchronousQueue（公平模式）有什么异同呢？
 * （1）在java8中两者的实现方式基本一致，都是使用的双重队列（节点是两种数据类型，生产者或消费者）；
 * （2）前者完全实现了后者，但比后者更灵活；
 * （3）后者不管放元素还是取元素，如果没有可匹配的元素，所在的线程都会阻塞；
 * （4）前者可以自己控制放元素是否需要阻塞线程，比如使用四个添加元素的方法就不会阻塞线程，只入队元素，使用transfer()会阻塞线程；
 * （5）取元素两者基本一样，都会阻塞等待有新的元素进入被匹配到；
 */
public class LinkedTransferQueue<E> extends AbstractQueue<E> implements TransferQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    /*
     * *** Overview of Dual Queues with Slack ***
     * A FIFO dual queue may be implemented using a variation of the Michael & Scott (M&S) lock-free queue algorithm
     *
     *  head                tail
     *    |                   |
     *    v                   v
     *    M -> U -> U -> U -> U
     *
     *
     * We introduce here an approach that lies between the extremes of
     * never versus always updating queue (head and tail) pointers.
     * This offers a tradeoff between sometimes requiring extra
     * traversal steps to locate the first and/or last unmatched
     * nodes, versus the reduced overhead and contention of fewer
     * updates to queue pointers. For example, a possible snapshot of
     * a queue is:
     *
     *  head           tail
     *    |              |
     *    v              v
     *    M -> M -> U -> U -> U -> U
     *
     */

    private static final boolean MP = Runtime.getRuntime().availableProcessors() > 1;

    private static final int FRONT_SPINS   = 1 << 7;

    private static final int CHAINED_SPINS = FRONT_SPINS >>> 1;
    // sweepVotes的阈值
    static final int SWEEP_THRESHOLD = 32;

    /**
     * 单向链表
     */
    static final class Node {
        // 是否是数据节点（也就标识了是生产者还是消费者）
        final boolean isData;   // false if this is a request node
        // 元素的值
        volatile Object item;   // initially non-null if isData; CASed to match
        // 后继节点
        volatile Node next;
        // 等待线程
        volatile Thread waiter; // null until waiting

        // CAS methods for fields
        final boolean casNext(Node cmp, Node val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        final boolean casItem(Object cmp, Object val) {
            // assert cmp == null || cmp.getClass() != Node.class;
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        Node(Object item, boolean isData) {
            UNSAFE.putObject(this, itemOffset, item); // relaxed write
            this.isData = isData;
        }
        // 当前节点出队
        final void forgetNext() {
            UNSAFE.putObject(this, nextOffset, this);
        }

        final void forgetContents() {
            UNSAFE.putObject(this, itemOffset, this);
            UNSAFE.putObject(this, waiterOffset, null);
        }

        final boolean isMatched() {
            Object x = item;
            return (x == this) || ((x == null) == isData);
        }

        final boolean isUnmatchedRequest() {
            return !isData && item == null;
        }

        final boolean cannotPrecede(boolean haveData) {
            boolean d = isData;
            Object x;
            return d != haveData && (x = item) != this && (x != null) == d;
        }

        final boolean tryMatchData() {
            // assert isData;
            Object x = item;
            if (x != null && x != this && casItem(x, null)) {
                LockSupport.unpark(waiter);
                return true;
            }
            return false;
        }

        private static final long serialVersionUID = -3375979862319811754L;

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;
        private static final long waiterOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
                waiterOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("waiter"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
    // 头结点
    transient volatile Node head;
    // 尾结点
    private transient volatile Node tail;

    private transient volatile int sweepVotes;

    // CAS methods for fields
    private boolean casTail(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private boolean casHead(Node cmp, Node val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    private boolean casSweepVotes(int cmp, int val) {
        return UNSAFE.compareAndSwapInt(this, sweepVotesOffset, cmp, val);
    }

    // 放取元素的几种方式：
    // 立即返回，用于非超时的poll()和tryTransfer()方法中
    private static final int NOW   = 0; // for untimed poll, tryTransfer
    // 异步，不会阻塞，用于放元素时，因为内部使用无界单链表存储元素，不会阻塞放元素的过程
    private static final int ASYNC = 1; // for offer, put, add
    // 同步，调用的时候如果没有匹配到会阻塞直到匹配到为止
    private static final int SYNC  = 2; // for transfer, take
    // 超时，用于有超时的poll()和tryTransfer()方法中
    private static final int TIMED = 3; // for timed poll, tryTransfer

    @SuppressWarnings("unchecked")
    static <E> E cast(Object item) {
        // assert item == null || item.getClass() != Node.class;
        return (E) item;
    }

    /**
     * @param e 元素
     * @param haveData 是否是数据节点
     * @param how 放取元素的方式，上面提到的四种，NOW、ASYNC、SYNC、TIMED
     * @param nanos 超时时间
     *
     * 总体逻辑
     * （1）来了一个元素，我们先查看队列头的节点，是否与这个元素的模式一样；
     * （2）如果模式不一样，就尝试让他们匹配，如果头节点被别的线程先匹配走了，就尝试与头节点的下一个节点匹配，如此一直往后，直到匹配到或到链表尾为止；
     * （3）如果模式一样，或者到链表尾了，就尝试入队
     * （4）入队的时候有可能链表尾修改了，那就尾指针后移，再重新尝试入队，依此往复；
     * （5）入队成功了，就自旋或阻塞，阻塞了就等待被其它线程匹配到并唤醒；
     * （6）唤醒之后进入下一次循环就匹配到元素了，返回匹配到的元素；
     * （7）是否需要入队及阻塞有四种情况：
     *     a）NOW，立即返回，没有匹配到立即返回，不做入队操作
     *        对应的方法有：poll()、tryTransfer(e)
     *     b）ASYNC，异步，元素入队但当前线程不会阻塞（相当于无界LinkedBlockingQueue的元素入队）
     *        对应的方法有：add(e)、offer(e)、put(e)、offer(e, timeout, unit)
     *     c）SYNC，同步，元素入队后当前线程阻塞，等待被匹配到
     *        对应的方法有：take()、transfer(e)
     *     d）TIMED，有超时，元素入队后等待一段时间被匹配，时间到了还没匹配到就返回元素本身
     *        对应的方法有：poll(timeout, unit)、tryTransfer(e, timeout, unit)
     */
    private E xfer(E e, boolean haveData, int how, long nanos) {
        // 不允许放入空元素
        if (haveData && (e == null))
            throw new NullPointerException();
        Node s = null;                        // the node to append, if needed
        // 外层循环，自旋，失败就重试
        retry:
        for (;;) {                            // restart on append race
            // 下面这个for循环用于控制匹配的过程
            // 同一时刻队列中只会存储一种类型的节点
            // 从头节点开始尝试匹配，如果头节点被其它线程先一步匹配了
            // 就再尝试其下一个，直到匹配到为止，或者到队列中没有元素为止
            for (Node h = head, p = h; p != null;) { // find & match first node
                // p节点的模式
                boolean isData = p.isData;
                // p节点的值
                Object item = p.item;
                // 判断节点是否被匹配过
                // item != null有2种情况：一是put操作，二是take的item被修改了(匹配成功)
                // (itme != null) == isData 要么表示p是一个put操作，要么表示p是一个还没匹配成功的take操作
                if (item != p && (item != null) == isData) { // unmatched
                    // 如果两者模式一样，则不能匹配，跳出循环后，继续自旋尝试入队
                    if (isData == haveData)   // can't match
                        break;
                    // 如果两者模式不一样，则尝试匹配
                    // 把p的值设置为e（如果是取元素则e是null，如果是放元素则e是元素值）
                    if (p.casItem(item, e)) { // match
                        // 匹配成功
                        // for里面的逻辑比较复杂，用于控制多线程同时放取元素时出现竞争的情况
                        for (Node q = p; q != h;) {
                            // 进入到这里可能是头节点已经被匹配，然后p会变成h的下一个节点
                            Node n = q.next;  // update by 2 unless singleton
                            // 如果head还没变，就把它更新成新的节点
                            // 并把它删除（forgetNext()会把它的next设为自己，也就是从单链表中删除了）
                            // 这时为什么要把head设为n呢？因为到这里了，肯定head本身已经被匹配掉了
                            // 而上面的p.casItem()又成功了，说明p也被当前这个元素给匹配掉了
                            // 所以需要把它们俩都出队列，让其它线程可以从真正的头开始，不用重复检查了
                            if (head == h && casHead(h, n == null ? q : n)) {
                                h.forgetNext();
                                break;
                            }                 // advance and retry
                            // 如果新的头节点为空，或者其next为空，或者其next未匹配，就重试
                            if ((h = head)   == null ||
                                (q = h.next) == null || !q.isMatched())
                                break;        // unless slack < 2
                        }
                        // 唤醒p中等待的线程
                        LockSupport.unpark(p.waiter);
                        // 返回匹配到的元素
                        return LinkedTransferQueue.<E>cast(item);
                    }
                }
                // p已经被匹配了或者尝试匹配的时候失败了
                // 也就是其它线程先一步匹配了p
                // 这时候又分两种情况，p的next还没来得及修改，p的next指向了自己
                // 如果p的next已经指向了自己，就重新取head重试，否则就取其next重试
                Node n = p.next;
                p = (p != n) ? n : (h = head); // Use head if p offlist
            }
            // 到这里肯定是队列中存储的节点类型和自己一样
            // 或者队列中没有元素了
            // 就入队（不管放元素还是取元素都得入队）
            // 入队又分成四种情况：
            // NOW，立即返回，没有匹配到立即返回，不做入队操作
            // ASYNC，异步，元素入队但当前线程不会阻塞（相当于无界LinkedBlockingQueue的元素入队）
            // SYNC，同步，元素入队后当前线程阻塞，等待被匹配到
            // TIMED，有超时，元素入队后等待一段时间被匹配，时间到了还没匹配到就返回元素本身
            if (how != NOW) {                 // No matches available
                // 新建s节点
                if (s == null)
                    s = new Node(e, haveData);
                // 尝试入队
                Node pred = tryAppend(s, haveData);
                // 入队失败，重试
                if (pred == null)
                    continue retry;           // lost race vs opposite mode
                // 如果不是异步（即同步或者有超时），就等待被匹配
                if (how != ASYNC)
                    return awaitMatch(s, pred, e, (how == TIMED), nanos);
            }
            return e; // not waiting
        }
    }
    // 节点入队
    private Node tryAppend(Node s, boolean haveData) {
        // 从tail开始遍历，把s放到链表尾端
        for (Node t = tail, p = t;;) {        // move p to last node and append
            Node n, u;                        // temps for reads of next & tail
            // 如果首尾都是null，说明链表中还没有元素
            if (p == null && (p = head) == null) {
                // 就让首节点指向s
                // 注意，这里插入第一个元素的时候tail指针并没有指向s
                if (casHead(null, s))
                    return s;                 // initialize
            }
            else if (p.cannotPrecede(haveData))
                // 如果p无法处理，则返回null
                // 这里无法处理的意思是，p和s节点的类型不一样，不允许s入队
                // 比如，其它线程先入队了一个数据节点，这时候要入队一个非数据节点，就不允许，
                // 队列中所有的元素都要保证是同一种类型的节点
                // 返回null后外面的方法会重新尝试匹配重新入队等
                return null;                  // lost race vs opposite mode
            else if ((n = p.next) != null)    // not last; keep traversing
                // 如果p的next不为空，说明不是最后一个节点
                // 则让p重新指向最后一个节点
                p = p != t && t != (u = tail) ? (t = u) : // stale tail
                    (p != n) ? n : null;      // restart if off list
            else if (!p.casNext(null, s))
                // 如果CAS更新s为p的next失败
                // 则说明有其它线程先一步更新到p的next了
                // 就让p指向p的next，重新尝试让s入队
                p = p.next;                   // re-read on CAS failure
            else {
                // 到这里说明s成功入队了
                // 如果p不等于t，就更新tail指针
                // 还记得上面插入第一个元素时tail指针并没有指向新元素吗？
                // 这里就是用来更新tail指针的
                if (p != t) {                 // update if slack now >= 2
                    while ((tail != t || !casTail(t, s)) &&
                           (t = tail)   != null &&
                           (s = t.next) != null && // advance and retry
                           (s = s.next) != null && s != t);
                }
                // 返回p，即s的前一个元素
                return p;
            }
        }
    }
    // 等待被匹配
    private E awaitMatch(Node s, Node pred, E e, boolean timed, long nanos) {
        // 如果是有超时，计算其超时时间
        final long deadline = timed ? System.nanoTime() + nanos : 0L;
        // 当前线程
        Thread w = Thread.currentThread();
        // 自旋次数
        int spins = -1; // initialized after first item and cancel checks
        // 随机数，随机让一些自旋的线程让出CPU
        ThreadLocalRandom randomYields = null; // bound if needed

        for (;;) {
            Object item = s.item;
            // 如果s元素的值不等于e，说明它被匹配到了
            if (item != e) {                  // matched
                // 把s的item更新为s本身
                // 并把s中的waiter置为空
                s.forgetContents();           // avoid garbage
                return LinkedTransferQueue.<E>cast(item);
            }
            // 如果当前线程中断了，或者有超时的到期了
            // 就更新s的元素值指向s本身
            if ((w.isInterrupted() || (timed && nanos <= 0)) && s.casItem(e, s)) { // cancel
                // 尝试解除s与其前一个节点的关系
                // 也就是删除s节点
                unsplice(pred, s);
                return e;
            }
            // 如果自旋次数小于0，就计算自旋次数
            if (spins < 0) {                  // establish spins at/near front
                // spinsFor()计算自旋次数
                // 如果前面有节点未被匹配就返回0
                // 如果前面有节点且正在匹配中就返回一定的次数，等待
                if ((spins = spinsFor(pred, s.isData)) > 0)
                    randomYields = ThreadLocalRandom.current();
            }
            else if (spins > 0) {
                // 还有自旋次数就减1
                --spins;
                // 并随机让出CPU
                if (randomYields.nextInt(CHAINED_SPINS) == 0)
                    Thread.yield();           // occasionally yield
            }
            else if (s.waiter == null) {
                // 更新s的waiter为当前线程
                s.waiter = w;                 // request unpark then recheck
            }
            else if (timed) {
                // 如果有超时，计算超时时间，并阻塞一定时间
                nanos = deadline - System.nanoTime();
                if (nanos > 0L)
                    LockSupport.parkNanos(this, nanos);
            }
            else {
                // 不是超时的，直接阻塞，等待被唤醒
                // 唤醒后进入下一次循环，走第一个if的逻辑就返回匹配的元素了
                LockSupport.park(this);
            }
        }
    }

    private static int spinsFor(Node pred, boolean haveData) {
        if (MP && pred != null) {
            if (pred.isData != haveData)      // phase change
                return FRONT_SPINS + CHAINED_SPINS;
            if (pred.isMatched())             // probably at front
                return FRONT_SPINS;
            if (pred.waiter == null)          // pred apparently spinning
                return CHAINED_SPINS;
        }
        return 0;
    }

    /* -------------- Traversal methods -------------- */

    final Node succ(Node p) {
        Node next = p.next;
        return (p == next) ? head : next;
    }

    private Node firstOfMode(boolean isData) {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return (p.isData == isData) ? p : null;
        }
        return null;
    }

    final Node firstDataNode() {
        for (Node p = head; p != null;) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p)
                    return p;
            }
            else if (item == null)
                break;
            if (p == (p = p.next))
                p = head;
        }
        return null;
    }

    private E firstDataItem() {
        for (Node p = head; p != null; p = succ(p)) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p)
                    return LinkedTransferQueue.<E>cast(item);
            }
            else if (item == null)
                return null;
        }
        return null;
    }

    private int countOfMode(boolean data) {
        int count = 0;
        for (Node p = head; p != null; ) {
            if (!p.isMatched()) {
                if (p.isData != data)
                    return 0;
                if (++count == Integer.MAX_VALUE) // saturated
                    break;
            }
            Node n = p.next;
            if (n != p)
                p = n;
            else {
                count = 0;
                p = head;
            }
        }
        return count;
    }

    final class Itr implements Iterator<E> {
        private Node nextNode;   // next node to return item for
        private E nextItem;      // the corresponding item
        private Node lastRet;    // last returned node, to support remove
        private Node lastPred;   // predecessor to unlink lastRet

        private void advance(Node prev) {
            Node r, b; // reset lastPred upon possible deletion of lastRet
            if ((r = lastRet) != null && !r.isMatched())
                lastPred = r;    // next lastPred is old lastRet
            else if ((b = lastPred) == null || b.isMatched())
                lastPred = null; // at start of list
            else {
                Node s, n;       // help with removal of lastPred.next
                while ((s = b.next) != null &&
                       s != b && s.isMatched() &&
                       (n = s.next) != null && n != s)
                    b.casNext(s, n);
            }

            this.lastRet = prev;

            for (Node p = prev, s, n;;) {
                s = (p == null) ? head : p.next;
                if (s == null)
                    break;
                else if (s == p) {
                    p = null;
                    continue;
                }
                Object item = s.item;
                if (s.isData) {
                    if (item != null && item != s) {
                        nextItem = LinkedTransferQueue.<E>cast(item);
                        nextNode = s;
                        return;
                    }
                }
                else if (item == null)
                    break;
                // assert s.isMatched();
                if (p == null)
                    p = s;
                else if ((n = s.next) == null)
                    break;
                else if (s == n)
                    p = null;
                else
                    p.casNext(s, n);
            }
            nextNode = null;
            nextItem = null;
        }

        Itr() {
            advance(null);
        }

        public final boolean hasNext() {
            return nextNode != null;
        }

        public final E next() {
            Node p = nextNode;
            if (p == null) throw new NoSuchElementException();
            E e = nextItem;
            advance(p);
            return e;
        }

        public final void remove() {
            final Node lastRet = this.lastRet;
            if (lastRet == null)
                throw new IllegalStateException();
            this.lastRet = null;
            if (lastRet.tryMatchData())
                unsplice(lastPred, lastRet);
        }
    }

    static final class LTQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final LinkedTransferQueue<E> queue;
        Node current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        LTQSpliterator(LinkedTransferQueue<E> queue) {
            this.queue = queue;
        }

        public Spliterator<E> trySplit() {
            Node p;
            final LinkedTransferQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null) &&
                p.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                do {
                    Object e = p.item;
                    if (e != p && (a[i] = e) != null)
                        ++i;
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (p != null && i < n && p.isData);
                if ((current = p) == null)
                    exhausted = true;
                if (i > 0) {
                    batch = i;
                    return Spliterators.spliterator
                        (a, 0, i, Spliterator.ORDERED | Spliterator.NONNULL |
                         Spliterator.CONCURRENT);
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            Node p;
            if (action == null) throw new NullPointerException();
            final LinkedTransferQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null)) {
                exhausted = true;
                do {
                    Object e = p.item;
                    if (e != null && e != p)
                        action.accept((E)e);
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (p != null && p.isData);
            }
        }

        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super E> action) {
            Node p;
            if (action == null) throw new NullPointerException();
            final LinkedTransferQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.firstDataNode()) != null)) {
                Object e;
                do {
                    if ((e = p.item) == p)
                        e = null;
                    if (p == (p = p.next))
                        p = q.firstDataNode();
                } while (e == null && p != null && p.isData);
                if ((current = p) == null)
                    exhausted = true;
                if (e != null) {
                    action.accept((E)e);
                    return true;
                }
            }
            return false;
        }

        public long estimateSize() { return Long.MAX_VALUE; }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.NONNULL |
                Spliterator.CONCURRENT;
        }
    }

    public Spliterator<E> spliterator() {
        return new LTQSpliterator<E>(this);
    }

    /* -------------- Removal methods -------------- */
    // 节点s和其前继节点的链接
    final void unsplice(Node pred, Node s) {
        // 设置item自连接，waiter为null
        s.forgetContents(); // forget unneeded fields
        if (pred != null && pred != s && pred.next == s) {
            // 获取s的后继节点
            Node n = s.next;
            // s的后继节点为null，或不为null，就将s的前驱节点的后继节点设置为n
            if (n == null ||
                (n != s && pred.casNext(s, n) && pred.isMatched())) {
                for (;;) {               // check if at, or could be, head
                    Node h = head;
                    if (h == pred || h == s || h == null)
                        return;          // at head or list empty
                    if (!h.isMatched())
                        break;
                    Node hn = h.next;
                    if (hn == null)
                        return;          // now empty
                    if (hn != h && casHead(h, hn))
                        h.forgetNext();  // advance head
                }
                if (pred.next != pred && s.next != s) { // recheck if offlist
                    for (;;) {           // sweep now if enough votes
                        int v = sweepVotes;
                        if (v < SWEEP_THRESHOLD) {
                            if (casSweepVotes(v, v + 1))
                                break;
                        }
                        // 达到阀值，进行“大扫除”，清除队列中的无效节点
                        else if (casSweepVotes(v, 0)) {
                            sweep();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void sweep() {
        for (Node p = head, s, n; p != null && (s = p.next) != null; ) {
            if (!s.isMatched())
                // Unmatched nodes are never self-linked
                p = s;
            else if ((n = s.next) == null) // trailing node is pinned
                break;
            else if (s == n)    // stale
                // No need to also check for p == s, since that implies s == n
                p = head;
            else
                p.casNext(s, n);
        }
    }

    private boolean findAndRemove(Object e) {
        if (e != null) {
            for (Node pred = null, p = head; p != null; ) {
                Object item = p.item;
                if (p.isData) {
                    if (item != null && item != p && e.equals(item) &&
                        p.tryMatchData()) {
                        unsplice(pred, p);
                        return true;
                    }
                }
                else if (item == null)
                    break;
                pred = p;
                if ((p = p.next) == pred) { // stale
                    pred = null;
                    p = head;
                }
            }
        }
        return false;
    }
    // 无初始容量
    public LinkedTransferQueue() {
    }

    public LinkedTransferQueue(Collection<? extends E> c) {
        this();
        addAll(c);
    }
    // 异步模式，不会阻塞，不会超时，put、offer、add使用异步的方式调用xfer()方法，传入的参数都一模一样
    // 因为是放元素，单链表存储，会一直往后加
    public void put(E e) {
        xfer(e, true, ASYNC, 0);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    public boolean offer(E e) {
        xfer(e, true, ASYNC, 0);
        return true;
    }

    public boolean add(E e) {
        xfer(e, true, ASYNC, 0);
        return true;
    }
    // 移交元素， xfer方法第二个参数都是true，也就是这三个方法其实也是放元素的方法。
    public boolean tryTransfer(E e) {
        // 立即返回
        return xfer(e, true, NOW, 0) == null;
    }
    public void transfer(E e) throws InterruptedException {
        // 同步模式
        if (xfer(e, true, SYNC, 0) != null) {
            Thread.interrupted(); // failure possible only due to interrupt
            throw new InterruptedException();
        }
    }
    public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        // 有超时时间
        if (xfer(e, true, TIMED, unit.toNanos(timeout)) == null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    // 出队，取元素
    // 出队的四个方法也是直接或间接的调用xfer()方法，放取元素的方式和超时规则略微不同，本质没有大的区别

    // 同步模式，会阻塞直到取到元素
    public E take() throws InterruptedException {
        E e = xfer(null, false, SYNC, 0);
        if (e != null)
            return e;
        Thread.interrupted();
        throw new InterruptedException();
    }
    // 有超时时间
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = xfer(null, false, TIMED, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }
    // 立即返回，没取到元素返回null
    public E poll() {
        return xfer(null, false, NOW, 0);
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int n = 0;
        for (E e; n < maxElements && (e = poll()) != null;) {
            c.add(e);
            ++n;
        }
        return n;
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public E peek() {
        return firstDataItem();
    }

    public boolean isEmpty() {
        for (Node p = head; p != null; p = succ(p)) {
            if (!p.isMatched())
                return !p.isData;
        }
        return true;
    }

    public boolean hasWaitingConsumer() {
        return firstOfMode(false) != null;
    }

    public int size() {
        return countOfMode(true);
    }

    public int getWaitingConsumerCount() {
        return countOfMode(false);
    }

    public boolean remove(Object o) {
        return findAndRemove(o);
    }

    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node p = head; p != null; p = succ(p)) {
            Object item = p.item;
            if (p.isData) {
                if (item != null && item != p && o.equals(item))
                    return true;
            }
            else if (item == null)
                break;
        }
        return false;
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        for (E e : this)
            s.writeObject(e);
        // Use trailing null as sentinel
        s.writeObject(null);
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        for (;;) {
            @SuppressWarnings("unchecked")
            E item = (E) s.readObject();
            if (item == null)
                break;
            else
                offer(item);
        }
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long sweepVotesOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = LinkedTransferQueue.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
            sweepVotesOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("sweepVotes"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
