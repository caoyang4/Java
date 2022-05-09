package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * SynchronousQueue是BlockingQueue的一种，所以SynchronousQueue是线程安全的。
 * SynchronousQueue和其他的BlockingQueue不同的是SynchronousQueue的capacity是0。即SynchronousQueue不存储任何元素。
 * 每一个put操作必须等待一个take操作，否则不能继续添加元素，可用于数据的传递
 * SynchronousQueue支持公平性和非公平性2种策略来访问队列。默认是采用非公平性策略访问队列。
 * 公平性策略底层使用了类似队列的数据结构，而非公平策略底层使用了类似栈的数据结构
 *
 * 和Exchanger不同的是，使用SynchronousQueue可以在两个线程中传递同一个对象。一个线程放对象，另外一个线程取对象。
 * 而Exchanger是线程间交换数据
 *
 * 实现原理
 * （1）定义了一个抽象类Transferer，里面定义了一个传输元素的方法；
 * （2）有两种传输元素的方法，一种是栈（非公平策略），一种是队列（公平策略）；
 * （3）栈的特点是后进先出，队列的特点是先进行出；
 * （4）栈只需要保存一个头节点就可以了，因为存取元素都是操作头节点；
 * （5）队列需要保存一个头节点一个尾节点，因为存元素操作尾节点，取元素操作头节点；
 *
 * SynchronousQueue单纯的放元素，一定是会被阻塞的，即放元素这个操作不能结束。
 * 直到有其它线程来取元素时，一放一取的情况下，两者都才能成功，操作一定是成对存在的。
 */
public class SynchronousQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = -3223113410248163686L;

    abstract static class Transferer<E> {
        // 三个参数分别是：传输的元素，是否需要超时，超时的时间
        abstract E transfer(E e, boolean timed, long nanos);
    }

    static final int NCPUS = Runtime.getRuntime().availableProcessors();
    // 最大自旋次数
    // 只有一个cpu时,自选次数为 0， 直接选择 LockSupport.park() 挂起等待者线程
    static final int maxTimedSpins = (NCPUS < 2) ? 0 : 32;

    static final int maxUntimedSpins = maxTimedSpins * 16;
    // 如果请求是指定超时限制的话，如果超时nanos参数是< 1000 纳秒时，
    // 禁止挂起。挂起再唤醒的成本太高了..还不如选择自旋空转呢...
    static final long spinForTimeoutThreshold = 1000L;
    // 非公平策略的核心类，可以使用它实现非公平的访问队列
    static final class TransferStack<E> extends Transferer<E> {
        // 表示Node类型为 请求类型(消费者)
        static final int REQUEST    = 0;
        // 表示Node类型为 数据类型（生产者）
        static final int DATA       = 1;
        /**
         * 表示Node类型为 匹配中类型
         * 假设栈顶元素为 REQUEST-NODE，当前请求类型为 DATA的话，入栈会修改类型为 FULFILLING 【栈顶 & 栈顶之下的一个node】。
         * 假设栈顶元素为 DATA-NODE，当前请求类型为 REQUEST的话，入栈会修改类型为 FULFILLING 【栈顶 & 栈顶之下的一个node】。
         */
        static final int FULFILLING = 2;
        // 判断当前模式是否为 匹配中状态
        static boolean isFulfilling(int m) { return (m & FULFILLING) != 0; }

        // 单向链表
        static final class SNode {
            // 后继节点
            volatile SNode next;        // next node in stack
            //与当前node匹配的节点
            /**
             * null：还没有任何匹配
             * 等于自己：表示当前为取消状态
             * 等于别的Snode：当前为匹配状态
             */
            volatile SNode match;       // the node matched to this
            // 等待线程
            volatile Thread waiter;     // to control park/unpark

            /**
             * item 和 mode 都不是volatile，因为读写操作被其他volatile/atomic数据保护
             */
            // 数据域，data不为空 表示当前Node对应的请求类型为 DATA类型。 反之则表示Node为 REQUEST类型。
            Object item;                // data; or null for REQUESTs
            int mode;

            SNode(Object item) {this.item = item;}
            // CAS方式设置Node对象的next字段
            boolean casNext(SNode cmp, SNode val) {
                return cmp == next && UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }
            /**
             * 尝试匹配
             * 调用tryMatch的对象是 栈顶节点的下一个节点，与栈顶匹配的节点。
             */
            boolean tryMatch(SNode s) {
                // match == null 成立，说明当前Node尚未与任何节点发生过匹配...

                if (match == null &&
                    // 使用CAS方式 设置match字段为null，表示当前Node已经被匹配了
                    UNSAFE.compareAndSwapObject(this, matchOffset, null, s)) {
                    Thread w = waiter;
                    if (w != null) {    // waiters need at most one unpark
                        waiter = null;
                        // 唤醒等待线程
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                // 可能其它线程先一步匹配了m，返回其是否是s
                return match == s;
            }
            // match字段保留当前Node对象本身，表示这个Node是取消状态，取消状态的Node，最终会被强制移除出栈
            void tryCancel() {
                UNSAFE.compareAndSwapObject(this, matchOffset, null, this);
            }

            boolean isCancelled() {
                return match == this;
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long matchOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = SNode.class;
                    matchOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("match"));
                    nextOffset = UNSAFE.objectFieldOffset(k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }
        // 头结点
        volatile SNode head;
        // 设置头结点
        boolean casHead(SNode h, SNode nh) {
            return h == head && UNSAFE.compareAndSwapObject(this, headOffset, h, nh);
        }

        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) s = new SNode(e);
            s.mode = mode;
            s.next = next;
            return s;
        }
        /**
         * 非公平的 transfer 方法
         * （1）如果栈中没有元素，或者栈顶元素跟将要入栈的元素模式一样，就入栈；
         * （2）入栈后自旋等待一会看有没有其它线程匹配到它，自旋完了还没匹配到元素就阻塞等待；
         * （3）阻塞等待被唤醒了说明其它线程匹配到了当前的元素，就返回匹配到的元素；
         * （4）如果两者模式不一样，且头节点没有在匹配中，就拿当前节点跟它匹配，匹配成功了就返回匹配到的元素；
         * （5）如果两者模式不一样，且头节点正在匹配中，当前线程就协助去匹配，匹配完成了再让当前节点重新入栈重新匹配；
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            SNode s = null; // constructed/reused as needed
            // 根据e是否为null决定是生产者还是消费者
            int mode = (e == null) ? REQUEST : DATA;

            for (;;) {
                // 栈顶元素，即头结点
                SNode h = head;
                // 栈顶没有元素，或者栈顶元素跟当前元素是一个模式的
                // 也就是都是生产者节点或者都是消费者节点
                if (h == null || h.mode == mode) {  // empty or same-mode
                    // 有超时且已到期
                    if (timed && nanos <= 0) {      // can't wait
                        // 如果头节点不为空且是取消状态
                        if (h != null && h.isCancelled())
                            // 就把头节点弹出，并进入下一次循环
                            casHead(h, h.next);     // pop cancelled node
                        else
                            // 否则，直接返回null（超时返回null）
                            return null;
                    }
                    // 当前栈顶为空 或者 模式与当前请求一致，且当前请求允许阻塞等待。
                    // snode(s, e, h, mode),将当前的信息包装为一个Snode节点
                    // casHead(h, s = snode(s, e, h, mode))  入栈操作。
                    else if (casHead(h, s = snode(s, e, h, mode))) {
                        // 执行到这里，说明 当前请求入栈成功，然后等待被匹配
                        // awaitFulfill 等待被匹配的逻辑...
                        //  1.正常情况：返回匹配的节点
                        //  2.取消情况：返回当前节点，s节点进去，返回s节点...
                        SNode m = awaitFulfill(s, timed, nanos);
                        // 当前Node状态是取消状态
                        if (m == s) {               // wait was cancelled
                            // 将取消状态的节点出栈，并返回null
                            clean(s);
                            return null;
                        }
                        // 执行到这里，说明当前Node已经被匹配了
                        // 头结点不为空，并且头节点的下一个节点是s
                        if ((h = head) != null && h.next == s)
                            casHead(h, s.next);     // help s's fulfiller
                        // 当前node模式为REQUEST类型：返回匹配节点的m.item 数据域
                        // 当前node模式为DATA类型：返回Node.item 数据域，当前请求提交的数据 e
                        return (E) ((mode == REQUEST) ? m.item : s.item);
                    }
                  // 到这里说明头节点和当前节点模式不一样
                  // 如果头节点不是正在匹配中
                } else if (!isFulfilling(h.mode)) { // try to fulfill
                    // 如果头节点已经取消了，就把它弹出栈
                    if (h.isCancelled())            // already cancelled
                        casHead(h, h.next);         // pop and retry
                    else if (casHead(h, s=snode(s, e, h, FULFILLING|mode))) {
                        // 头节点没有在匹配中，就让当前节点先入队，再让他们尝试匹配
                        // 且s成为了新的头节点，它的状态是正在匹配中
                        for (;;) { // loop until matched or waiters disappear
                            SNode m = s.next;       // m is s's match
                            // 如果m为null，说明除了s节点外的节点都被其它线程先一步匹配掉了
                            // 就清空栈并跳出内部循环，到外部循环再重新入栈判断
                            if (m == null) {        // all waiters are gone
                                casHead(s, null);   // pop fulfill node
                                s = null;           // use new node next time
                                break;              // restart main loop
                            }
                            SNode mn = m.next;
                            // 如果m和s尝试匹配成功，就弹出栈顶的两个元素m和s
                            if (m.tryMatch(s)) {
                                casHead(s, mn);     // pop both s and m
                                return (E) ((mode == REQUEST) ? m.item : s.item);
                            } else
                                // 尝试匹配失败，说明m已经先一步被其它线程匹配了
                                // 就协助清除它
                                s.casNext(m, mn);   // help unlink
                        }
                    }
                } else {
                    // 到这里说明当前节点和头节点模式不一样
                    // 且头节点是正在匹配中// help a fulfiller
                    SNode m = h.next;               // m is h's match
                    if (m == null)                  // waiter is gone
                        // 如果m为null，说明m已经被其它线程先一步匹配了
                        casHead(h, null);           // pop fulfilling node
                    else {
                        SNode mn = m.next;
                        // 协助匹配，如果m和s尝试匹配成功，就弹出栈顶的两个元素m和s
                        if (m.tryMatch(h))          // help match
                            // 将栈顶的两个元素弹出后，再让s重新入栈
                            casHead(h, mn);         // pop both h and m
                        else                        // lost match
                            // 尝试匹配失败，说明m已经先一步被其它线程匹配了
                            // 就协助清除它
                            h.casNext(m, mn);       // help unlink
                    }
                }
            }
        }

        /**
         * @param s 需要等待的节点
         * @param timed 是否需要超时
         * @param nanos 超时时间
         */
        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            // 到期时间
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Thread w = Thread.currentThread();
            // 自旋次数
            int spins = (shouldSpin(s) ? (timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                // 当前线程中断了，尝试清除s
                if (w.isInterrupted())
                    s.tryCancel();
                // 检查s是否匹配到了元素m（有可能是其它线程的m匹配到当前线程的s）
                SNode m = s.match;
                // 如果匹配到了，直接返回m
                if (m != null)
                    return m;
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    // 检查超时时间如果小于0，尝试清除s
                    if (nanos <= 0L) {
                        s.tryCancel();
                        continue;
                    }
                }
                if (spins > 0)
                    // 如果还有自旋次数，自旋次数减一，并进入下一次自旋
                    spins = shouldSpin(s) ? (spins-1) : 0;
                // 后面的elseif都是自旋次数没有了
                else if (s.waiter == null)
                    // 如果s的waiter为null，把当前线程注入进去，并进入下一次自旋
                    s.waiter = w; // establish waiter so can park next iter
                else if (!timed)
                    // 如果不允许超时，直接阻塞，并等待被其它线程唤醒，唤醒后继续自旋并查看是否匹配到了元素
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    // 如果允许超时且还有剩余时间，就阻塞相应时间
                    LockSupport.parkNanos(this, nanos);
            }
        }

        boolean shouldSpin(SNode s) {
            SNode h = head;
            return (h == s || h == null || isFulfilling(h.mode));
        }

        void clean(SNode s) {
            s.item = null;   // forget item
            s.waiter = null; // forget thread

            SNode past = s.next;
            if (past != null && past.isCancelled())
                past = past.next;

            // Absorb cancelled nodes at head
            SNode p;
            while ((p = head) != null && p != past && p.isCancelled())
                casHead(p, p.next);

            // Unsplice embedded nodes
            while (p != null && p != past) {
                SNode n = p.next;
                if (n != null && n.isCancelled())
                    p.casNext(n, n.next);
                else
                    p = n;
            }
        }

        // Unsafe mechanics
        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferStack.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }
    // 公平策略核心类
    static final class TransferQueue<E> extends Transferer<E> {

        static final class QNode {
            // 后继节点
            volatile QNode next;          // next node in queue
            // 存储的数据
            volatile Object item;         // CAS'ed to or from null
            // 等待线程
            volatile Thread waiter;       // to control park/unpark
            // 是否是数据节点
            final boolean isData;

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) {
                return next == cmp &&
                    UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
            }

            boolean casItem(Object cmp, Object val) {
                return item == cmp &&
                    UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
            }

            void tryCancel(Object cmp) {
                UNSAFE.compareAndSwapObject(this, itemOffset, cmp, this);
            }

            boolean isCancelled() {
                return item == this;
            }

            boolean isOffList() {
                return next == this;
            }

            // Unsafe mechanics
            private static final sun.misc.Unsafe UNSAFE;
            private static final long itemOffset;
            private static final long nextOffset;

            static {
                try {
                    UNSAFE = sun.misc.Unsafe.getUnsafe();
                    Class<?> k = QNode.class;
                    itemOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("item"));
                    nextOffset = UNSAFE.objectFieldOffset
                        (k.getDeclaredField("next"));
                } catch (Exception e) {
                    throw new Error(e);
                }
            }
        }
        // 公平队列头结点
        transient volatile QNode head;
        // 公平队列尾结点
        transient volatile QNode tail;
        // 表示被清理节点的前驱节点
        transient volatile QNode cleanMe;

        TransferQueue() {
            QNode h = new QNode(null, false); // initialize to dummy node.
            head = h;
            tail = h;
        }
        // 设置头指针指向新的节点，蕴含操作：老的头节点出队
        void advanceHead(QNode h, QNode nh) {
            if (h == head &&
                UNSAFE.compareAndSwapObject(this, headOffset, h, nh))
                h.next = h; // forget old next
        }

        void advanceTail(QNode t, QNode nt) {
            if (tail == t)
                UNSAFE.compareAndSwapObject(this, tailOffset, t, nt);
        }

        boolean casCleanMe(QNode cmp, QNode val) {
            return cleanMe == cmp &&
                UNSAFE.compareAndSwapObject(this, cleanMeOffset, cmp, val);
        }

        /**
         * 公平策略的 transfer 方法, 主要分为两种情况
         * 1. 若队列为空 / 队列中的尾节点和自己的 类型相同, 则添加 node
         *      到队列中, 直到 timeout/interrupt/其他线程和这个线程匹配
         *      timeout/interrupt awaitFulfill方法返回的是 node 本身
         *      匹配成功的话, 要么返回 null (producer返回的), 或正真的传递值 (consumer 返回的)
         * 2. 队列不为空, 且队列的 head.next 节点是当前节点匹配的节点,
         *      进行数据的传递匹配, 并且通过 advanceHead 方法帮助 先前 block 的节点 dequeue
         */
        @SuppressWarnings("unchecked")
        E transfer(E e, boolean timed, long nanos) {
            QNode s = null; // constructed/reused as needed
            // 1.判断 e != null 用于区分生产者与消费者
            boolean isData = (e != null);

            for (;;) {
                QNode t = tail;
                QNode h = head;
                // tail 和 head 没有初始化时，无限循环
                // 虽然这种 continue 非常耗cpu，但基本不会碰到这种情况
                // 因为 tail 和 head 在 TransferQueue 初始化的时候，就已经被赋值空节点了
                if (t == null || h == null)         // saw uninitialized value
                    continue;                       // spin
                // 首尾节点相同，说明是空队列
                // 或者尾节点的操作和当前节点操作一致
                if (h == t || t.isData == isData) { // empty or same-mode
                    QNode tn = t.next;
                    // 当 t 不是 tail 时，说明 tail 已经被其他线程修改过了
                    if (t != tail)                  // inconsistent read
                        continue;
                    // 队尾的后继节点还不为空，t 还不是队尾，直接把 tn 赋值给 t
                    if (tn != null) {               // lagging tail
                        advanceTail(t, tn);
                        continue;
                    }
                    //超时直接返回 null
                    if (timed && nanos <= 0)        // can't wait
                        return null;
                    // 构造node节点
                    if (s == null) s = new QNode(e, isData);
                    // 如果把 e 放到队尾失败，自旋
                    if (!t.casNext(null, s))        // failed to link in
                        continue;
                    // 更新队尾为请求节点
                    advanceTail(t, s);              // swing tail and wait
                    // 当前节点 等待匹配
                    // 当前请求为DATA模式时：e == 生产者传进来的数据
                    // x == this 当前SNode对应的线程 取消状态
                    // x == null 表示已经有匹配节点了，并且匹配节点拿走了item数据。

                    // 当前请求为REQUEST模式时：e == null
                    // x == this 当前SNode对应的线程 取消状态
                    // x != null 且 item != this  表示当前REQUEST类型的Node已经匹配到一个DATA类型的Node了。
                    Object x = awaitFulfill(s, e, timed, nanos);
                    if (x == s) {
                        // 说明当前Node状态为取消状态，需要做出队逻辑
                        clean(t, s);
                        return null;
                    }
                    // 执行到这里说明 当前Node匹配成功了
                    // 1.当前线程在awaitFulfill方法内，之前被挂起，此时运行到这里时是被匹配节点的线程使用LockSupport.unpark() 唤醒的
                    // 被唤醒：当前请求对应的节点，肯定已经出队了，因为匹配者线程 是先让当前Node出队的，再唤醒当前Node对应线程的。

                    // 2.当前线程在awaitFulfill方法内，处于自旋状态...此时匹配节点 匹配后，它检查发现了，然后返回到上层transfer方法的。
                    // 自旋状态返回时：当前请求对应的节点，不一定就出队了...

                    // 被唤醒时：s.isOffList() 条件会成立。  !s.isOffList() 不会成立。
                    // 条件成立：说明当前Node仍然在队列内，需要做 匹配成功后 出队逻辑。
                    if (!s.isOffList()) {           // not already unlinked
                        // 防止当前Node是自旋检查状态时发现被匹配了，然后当前线程需要将对应的Node做出队逻辑
                        advanceHead(t, s);          // unlink if head
                        // t 当前s节点的前驱节点，更新dummy节点为 s节点。表示head.next节点已经出队了
                        if (x != null)              // and forget fields
                            s.item = s;
                        s.waiter = null;
                    }
                    // x != null 成立，说明当前请求是REQUEST类型，返回匹配到的数据x
                    // x != null 不成立，说明当前请求是DATA类型，返回DATA请求时的e。
                    return (x != null) ? (E)x : e;
                  // 队尾节点 与 当前请求节点互补 （
                  // 队尾->DATA，请求类型->REQUEST）
                  // (队尾->REQUEST, 请求类型->DATA)
                } else {                            // complementary-mode
                    // h.next节点其实是真正的队头，请求节点与队尾模式不同，需要与队头发生匹配。因为TransferQueue是一个 公平模式
                    QNode m = h.next;               // node to fulfill
                    // 条件一：t != tail 什么时候成立呢？ 肯定是并发导致的，其它线程已经修改过tail了，有其它线程入队过了.,
                    //        当前线程看到的是过期数据，需要重新循环
                    // 条件二：m == null 什么时候成立呢？ 肯定是其它请求先当前请求一步，匹配走了head.next节点，重新循环
                    // 条件三：条件成立，说明已经有其它请求匹配走head.next了，当前线程看到的是过期数据，重新循环
                    if (t != tail || m == null || h != head)
                        continue;                   // inconsistent read

                    Object x = m.item;
                    //条件一：isData == (x != null)
                    //isData 表示当前请求是什么类型  isData == true：当前请求是DATA类型  isData == false：当前请求是REQUEST类型。
                    //1.假设isData == true   DATA类型
                    //m其实表示的是 REQUEST 类型的NODE，它的数据域是 null  => x==null
                    //true == (null != null)  => true == false => false

                    //2.假设isData == false REQUEST类型
                    //m其实表示的是 DATA 类型的NODE，它的数据域是 提交是的e ，并且e != null。
                    //false == (obj != null) => false == true => false

                    //总结：正常情况下，条件一不会成立。

                    //条件二：条件成立，说明m节点已经是 取消状态了...不能完成匹配，当前请求需要continue，再重新选择路径执行了..

                    //条件三：!m.casItem(x, e)，前提条件 m 非取消状态。
                    //1.假设当前请求为REQUEST类型   e == null
                    //m 是 DATA类型了...
                    //相当于将匹配的DATA Node的数据域清空了，相当于REQUEST 拿走了 它的数据。

                    //2.假设当前请求为DATA类型    e != null
                    //m 是 REQUEST类型了...
                    //相当于将匹配的REQUEST Node的数据域 填充了，填充了 当前DATA 的 数据。相当于传递给REQUEST请求数据了
                    if (isData == (x != null) ||    // m already fulfilled
                        x == m ||                   // m cancelled
                        !m.casItem(x, e)) {         // lost CAS
                        advanceHead(h, m);          // dequeue and retry
                        continue;
                    }
                    // 1.将真正的头节点 出队。让这个真正的头结点成为dummy节点
                    advanceHead(h, m);              // successfully fulfilled
                    LockSupport.unpark(m.waiter);
                    return (x != null) ? (E)x : e;
                }
            }
        }

        Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            /* Same idea as TransferStack.awaitFulfill */
            final long deadline = timed ? System.nanoTime() + nanos : 0L;
            Thread w = Thread.currentThread();
            int spins = ((head.next == s) ?
                         (timed ? maxTimedSpins : maxUntimedSpins) : 0);
            for (;;) {
                if (w.isInterrupted())
                    s.tryCancel(e);
                Object x = s.item;
                //item有几种情况呢？
                //当SNode模式为DATA模式时：
                //1.item != null 且 item != this  表示请求要传递的数据 put(E e)
                //2.item == this 当前SNode对应的线程 取消状态
                //3.item == null 表示已经有匹配节点了，并且匹配节点拿走了item数据。

                //当SNode模式为REQUEST模式时：
                //1.item == null 时，正常状态，当前请求仍然未匹配到对应的DATA请求。
                //2.item == this 当前SNode对应的线程 取消状态
                //3.item != null 且 item != this  表示当前REQUEST类型的Node已经匹配到一个DATA类型的Node了。

                //条件成立：
                //当前请求为DATA模式时：e 请求带来的数据
                //item == this 当前SNode对应的线程 取消状态
                //item == null 表示已经有匹配节点了，并且匹配节点拿走了item数据。

                //当前请求为REQUEST模式时：e == null
                //item == this 当前SNode对应的线程 取消状态
                //item != null 且 item != this  表示当前REQUEST类型的Node已经匹配到一个DATA类型的Node了。
                if (x != e)
                    return x;
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0L) {
                        s.tryCancel(e);
                        continue;
                    }
                }
                if (spins > 0)
                    --spins;
                else if (s.waiter == null)
                    s.waiter = w;
                else if (!timed)
                    LockSupport.park(this);
                else if (nanos > spinForTimeoutThreshold)
                    LockSupport.parkNanos(this, nanos);
            }
        }

        void clean(QNode pred, QNode s) {
            s.waiter = null; // forget thread
            while (pred.next == s) { // Return early if already unlinked
                QNode h = head;
                QNode hn = h.next;   // Absorb cancelled first node as head
                if (hn != null && hn.isCancelled()) {
                    advanceHead(h, hn);
                    continue;
                }
                QNode t = tail;      // Ensure consistent read for tail
                if (t == h)
                    return;
                QNode tn = t.next;
                if (t != tail)
                    continue;
                if (tn != null) {
                    advanceTail(t, tn);
                    continue;
                }
                if (s != t) {        // If not tail, try to unsplice
                    QNode sn = s.next;
                    if (sn == s || pred.casNext(s, sn))
                        return;
                }
                QNode dp = cleanMe;
                if (dp != null) {    // Try unlinking previous cancelled node
                    QNode d = dp.next;
                    QNode dn;
                    if (d == null ||               // d is gone or
                        d == dp ||                 // d is off list or
                        !d.isCancelled() ||        // d not cancelled or
                        (d != t &&                 // d not tail and
                         (dn = d.next) != null &&  //   has successor
                         dn != d &&                //   that is on list
                         dp.casNext(d, dn)))       // d unspliced
                        casCleanMe(dp, null);
                    if (dp == pred)
                        return;      // s is already saved node
                } else if (casCleanMe(null, pred))
                    return;          // Postpone cleaning s
            }
        }

        private static final sun.misc.Unsafe UNSAFE;
        private static final long headOffset;
        private static final long tailOffset;
        private static final long cleanMeOffset;
        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = TransferQueue.class;
                headOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("head"));
                tailOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("tail"));
                cleanMeOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("cleanMe"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    private transient volatile Transferer<E> transferer;
    // 默认非公平策略
    public SynchronousQueue() {
        this(false);
    }

    public SynchronousQueue(boolean fair) {
        transferer = fair ? new TransferQueue<E>() : new TransferStack<E>();
    }
    // 添加元素
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        // 传入元素e，说明是生产者
        if (transferer.transfer(e, false, 0) == null) {
            Thread.interrupted();
            throw new InterruptedException();
        }
    }
    // 超时添加
    public boolean offer(E e, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (e == null) throw new NullPointerException();
        if (transferer.transfer(e, true, unit.toNanos(timeout)) != null)
            return true;
        if (!Thread.interrupted())
            return false;
        throw new InterruptedException();
    }

    public boolean offer(E e) {
        if (e == null) throw new NullPointerException();
        return transferer.transfer(e, true, 0) != null;
    }

    public E take() throws InterruptedException {
        // 传入null表示是消费者，要取元素
        E e = transferer.transfer(null, false, 0);
        // 如果取到了元素就返回
        if (e != null)
            return e;
        // 否则让线程中断并抛出中断异常
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted())
            return e;
        throw new InterruptedException();
    }

    public E poll() {
        return transferer.transfer(null, true, 0);
    }

    public boolean isEmpty() {
        return true;
    }

    public int size() {
        return 0;
    }

    public int remainingCapacity() {
        return 0;
    }

    public void clear() {
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public E peek() {
        return null;
    }

    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    public Object[] toArray() {
        return new Object[0];
    }

    public <T> T[] toArray(T[] a) {
        if (a.length > 0)
            a[0] = null;
        return a;
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


    @SuppressWarnings("serial")
    static class WaitQueue implements java.io.Serializable { }
    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;
    }
    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;
    }
    private ReentrantLock qlock;
    private WaitQueue waitingProducers;
    private WaitQueue waitingConsumers;

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        boolean fair = transferer instanceof TransferQueue;
        if (fair) {
            qlock = new ReentrantLock(true);
            waitingProducers = new FifoWaitQueue();
            waitingConsumers = new FifoWaitQueue();
        }
        else {
            qlock = new ReentrantLock();
            waitingProducers = new LifoWaitQueue();
            waitingConsumers = new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (waitingProducers instanceof FifoWaitQueue)
            transferer = new TransferQueue<E>();
        else
            transferer = new TransferStack<E>();
    }

    // Unsafe mechanics
    static long objectFieldOffset(sun.misc.Unsafe UNSAFE,
                                  String field, Class<?> klazz) {
        try {
            return UNSAFE.objectFieldOffset(klazz.getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            // Convert Exception to corresponding Error
            NoSuchFieldError error = new NoSuchFieldError(field);
            error.initCause(e);
            throw error;
        }
    }

}
