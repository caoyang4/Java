package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * ConcurrentLinkedQueue 是一个由链表结构组成的无界非阻塞队列，
 * 为了减少 CAS 操作造成的资源争夺损耗，其链表结构被设计为“松弛”的Slack
 * ConcurrentLinkedQueue 是非阻塞队列，采用 CAS 和自旋保证并发安全。
 * ConcurrentLinkedQueue 的 tail 并不是严格指向尾节点，通过减少出队时对 tail 的 CAS 以提高效率。
 * ConcurrentLinkedQueue 的 head 所指节点可能是空节点，也可能是数据节点，通过减少出队时对 head 的 CAS 以提高效率。
 * 采用非阻塞算法，允许队列处于不一致状态（head/tail），通过保证不变式和可变式，来维护非阻塞算法的正确性。
 * 由于是非阻塞队列，无法使用在线程池中。
 */
public class ConcurrentLinkedQueue<E> extends AbstractQueue<E> implements Queue<E>, java.io.Serializable {
    private static final long serialVersionUID = 196745693267521676L;

    /**
     * 单向链表
     */
    private static class Node<E> {
        /**
         * item 为空表示无效节点，非空表示有效节点
         * ConcurrentLinkedQueue 队列中为什么要存储无效节点呢
         *   ，通过减少出队时对 head 的 CAS更新 以提高效率。
         */
        volatile E item;
        volatile Node<E> next;

        Node(E item) {
            UNSAFE.putObject(this, itemOffset, item);
        }

        boolean casItem(E cmp, E val) {
            return UNSAFE.compareAndSwapObject(this, itemOffset, cmp, val);
        }

        void lazySetNext(Node<E> val) {
            UNSAFE.putOrderedObject(this, nextOffset, val);
        }

        boolean casNext(Node<E> cmp, Node<E> val) {
            return UNSAFE.compareAndSwapObject(this, nextOffset, cmp, val);
        }

        // Unsafe mechanics

        private static final sun.misc.Unsafe UNSAFE;
        private static final long itemOffset;
        private static final long nextOffset;

        static {
            try {
                UNSAFE = sun.misc.Unsafe.getUnsafe();
                Class<?> k = Node.class;
                itemOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("item"));
                nextOffset = UNSAFE.objectFieldOffset
                    (k.getDeclaredField("next"));
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * head 和 tail 节点并不严格指向链表的头尾节点，
     * 也就是每次入队出队操作并不会及时更新 head 和 tail 节点
     *
     * head 的不变式：
     *  所有的有效节点，都能从 head 通过调用 succ() 方法遍历可达。
     *  head 不能为 null。
     *  head 节点的 next 域不能引用到自身。
     * head 的可变式：
     *  head 节点的 item 域可能为 null，也可能不为 null。
     *  允许 tail 滞后（lag behind）于 head，也就是说：从 head 开始遍历队列，不一定能到达 tail。
     */
    // 头结点
    private transient volatile Node<E> head;
    /**
     * tail 的不变式：
     *  通过 tail 调用 succ() 方法，最后节点总是可达的。
     *  tail 不能为 null。
     * tail 的可变式：
     *  tail 节点的 item 域可能为 null，也可能不为 null。
     *  允许 tail 滞后于 head，也就是说：从 head 开始遍历队列，不一定能到达 tail。
     *  tail 节点的 next 域可以引用到自身。
     */
    // 尾结点
    private transient volatile Node<E> tail;
    // 无参构造，默认创建空节点，head 和 tail 都指向该节点
    public ConcurrentLinkedQueue() {
        head = tail = new Node<E>(null);
    }

    public ConcurrentLinkedQueue(Collection<? extends E> c) {
        Node<E> h = null, t = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                t = newNode;
            }
        }
        if (h == null)
            h = t = new Node<E>(null);
        head = h;
        tail = t;
    }

    // 因为是无界队列，add(e)方法不用抛出异常。不支持添加 null
    public boolean add(E e) {
        return offer(e);
    }

    final void updateHead(Node<E> h, Node<E> p) {
        // 节点h和p不等，且当前头节点为h，则把头节点设为p
        if (h != p && casHead(h, p))
            // 原头节点h的next指向自身，表示h出队
            h.lazySetNext(h);
    }

    final Node<E> succ(Node<E> p) {
        Node<E> next = p.next;
        // 如果p已经出队了，则重新从头节点开始，否则继续遍历下一个节点
        return (p == next) ? head : next;
    }

    /**
     * 入队的基本思想：
     *  从 tail 节点开始遍历到尾节点，若定位到尾节点（p.next == null），则入队。
     *  遍历过程中，如果遍历到无效节点（p.next == p），需要重新从有效节点（tail 或 head）开始遍历。
     *  遍历过程中，时刻关注 tail 节点是否无效。若无效了需要重新从最新的 tail 开始遍历，否则继续遍历当前的下一个节点。
     *
     * 需要注意的点：
     *  入队过程中没有频繁执行 casTail（出队过程不会执行 casTail），因此 tail 位置有滞后，不一定指向尾节点，甚至可能位于废弃的链上。
     *  使用 p.next == null 来判断尾节点，比使用 tail 准确。
     *  通过 tail 遍历节点可能会遍历到无效节点，但是从 head 遍历总能访问到有效节点。
     */
    public boolean offer(E e) {
        checkNotNull(e);
        final Node<E> newNode = new Node<E>(e);
        // 注意tail不一定是尾节点（甚至tail有可能存在于废弃的链上，后有解释），但是也不妨从tail节点开始遍历链表
        for (Node<E> t = tail, p = t;;) {
            Node<E> q = p.next;
            // 使用p.next是否为空来判断p是否是尾节点，比较准确
            if (q == null) {
                // 若尾节点p的下一个节点为null，则设置为newNode
                if (p.casNext(null, newNode)) {
                    // 不管p与t是否相同，都应该casTail。
                    // 但是这里只在p与t不同时才casTail，导致tail节点不总是尾节点，目的是减少对tail的CAS
                    if (p != t) // hop two nodes at a time
                        // 将尾节点tail由t改为newNode，更新失败了也没关系，因为tail是不是尾节点不重要
                        casTail(t, newNode);  // Failure is OK.
                    return true;
                }
                // Lost CAS race to another thread; re-read next
            }
            // p已经出队了，需要重新设置p、t的值
            else if (p == q)
                // 1. 若节点t不再是tail，说明其他线程加入过元素(修改过tail)，则取最新tail作为t和p，从新的tail节点继续遍历链表
                // 2. 若节点t依旧是tail，说明从tail节点开始遍历链表已经不管用了，则把head作为p，从head节点从头遍历链表（注意这一步造成后续遍历中p!=t成立）
                p = (t != (t = tail)) ? t : head;
            else
                // 进入这里，说明p.next不为null，且p未出队，需要判断：
                // 1. 若p与t相等，则t留在原位，p=p.next一直往下遍历（注意这一步造成后续遍历中p!=t成立）。
                // 2. 若p与t不等，需进一步判断t与tail是否相等。若t不为tail，则取最新tail作为t和p；若t为tail，则p=p.next一直往下遍历。
                // 就是说从tail节点往后遍历链表的过程，需时刻关注tail是否发生变化
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }

    /**
     * 由于出队 poll() 逻辑并不会执行 casTail() 来维护 tail 所在位置，因此 tail 可能滞后于 head，甚至位于废弃链上
     *
     * 出队的基本思想：
     *   从 head 节点开始遍历找出首个有效节点（p.item != null），返回该节点的数据（p.item）。
     *   遍历过程中，如果遍历到尾节点（p.next == null），则返回空。
     *   遍历过程中，如果遍历到无效节点（p.next == p），说明其他线程修改了 head，需要重新从有效节点（新的 head）开始遍历。
     *
     * 需要注意的是，并不是每次出队时都执行 updateHead() 更新 head 节点：
     *   当 head 节点里有元素时，直接弹出 head 节点里的元素，而不会更新 head 节点。
     *   只有当 head 节点里没有元素时，出队操作才会更新 head 节点。
     * 采用这种方式同样是为了减少使用 CAS 更新 head 节点的消耗，从而提高出队效率。
     */
    public E poll() {
        restartFromHead:
        for (;;) {
            // 初始时h和p都指向head节点，从head节点开始遍历链表
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item;
                // p.item不为空，cas把p节点的数据域设为空，返回p节点的数据
                if (item != null && p.casItem(item, null)) {
                    // Successful CAS is the linearization point
                    // for item to be removed from this queue.
                    if (p != h) // hop two nodes at a time
                        // 若p.next不为空，则把p.next设为头节点，把h和p出队；
                        // 若p.next为空，则把p设为头节点，把h出队
                        updateHead(h, ((q = p.next) != null) ? q : p);
                    return item;
                }
                // 进入这里，说明p.item必然为空
                else if ((q = p.next) == null) {
                    // 若p.next也为空，说明队列中没有数据了，需要返回null
                    // 把头节点设为p，把h出队
                    updateHead(h, p);
                    return null;
                }
                // 如果p的next等于p，说明p已经出队了，重新从头节点开始遍历
                else if (p == q)
                    continue restartFromHead;
                else
                    // p = p.next 继续遍历链表
                    p = q;
            }
        }
    }

    public E peek() {
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                E item = p.item;
                if (item != null || (q = p.next) == null) {
                    updateHead(h, p);
                    return item;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }

    Node<E> first() {
        restartFromHead:
        for (;;) {
            for (Node<E> h = head, p = h, q;;) {
                boolean hasItem = (p.item != null);
                if (hasItem || (q = p.next) == null) {
                    updateHead(h, p);
                    return hasItem ? p : null;
                }
                else if (p == q)
                    continue restartFromHead;
                else
                    p = q;
            }
        }
    }

    public boolean isEmpty() {
        return first() == null;
    }

    /**
     * 获取队列的容量：从头开始遍历队列中的有效节点，并计数。注意是遍历过程是弱一致的
     */
    public int size() {
        int count = 0;
        // 从第一个有数据的节点开始，一直遍历链表
        for (Node<E> p = first(); p != null; p = succ(p))
            if (p.item != null)
                // Collection.size() spec says to max out
                if (++count == Integer.MAX_VALUE)
                    break;
        return count;
    }

    public boolean contains(Object o) {
        if (o == null) return false;
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null && o.equals(item))
                return true;
        }
        return false;
    }

    public boolean remove(Object o) {
        if (o != null) {
            Node<E> next, pred = null;
            for (Node<E> p = first(); p != null; pred = p, p = next) {
                boolean removed = false;
                E item = p.item;
                if (item != null) {
                    if (!o.equals(item)) {
                        next = succ(p);
                        continue;
                    }
                    removed = p.casItem(item, null);
                }

                next = succ(p);
                if (pred != null && next != null) // unlink
                    pred.casNext(p, next);
                if (removed)
                    return true;
            }
        }
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        if (c == this)
            // As historically specified in AbstractQueue#addAll
            throw new IllegalArgumentException();

        // Copy c into a private chain of Nodes
        Node<E> beginningOfTheEnd = null, last = null;
        for (E e : c) {
            checkNotNull(e);
            Node<E> newNode = new Node<E>(e);
            if (beginningOfTheEnd == null)
                beginningOfTheEnd = last = newNode;
            else {
                last.lazySetNext(newNode);
                last = newNode;
            }
        }
        if (beginningOfTheEnd == null)
            return false;

        // Atomically append the chain at the tail of this collection
        for (Node<E> t = tail, p = t;;) {
            Node<E> q = p.next;
            if (q == null) {
                // p is last node
                if (p.casNext(null, beginningOfTheEnd)) {
                    // Successful CAS is the linearization point
                    // for all elements to be added to this queue.
                    if (!casTail(t, last)) {
                        // Try a little harder to update tail,
                        // since we may be adding many elements.
                        t = tail;
                        if (last.next == null)
                            casTail(t, last);
                    }
                    return true;
                }
                // Lost CAS race to another thread; re-read next
            }
            else if (p == q)
                // We have fallen off list.  If tail is unchanged, it
                // will also be off-list, in which case we need to
                // jump to head, from which all live nodes are always
                // reachable.  Else the new tail is a better bet.
                p = (t != (t = tail)) ? t : head;
            else
                // Check for tail updates after two hops.
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }

    public Object[] toArray() {
        // Use ArrayList to deal with resizing.
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> p = first(); p != null; p = succ(p)) {
            E item = p.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray();
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        // try to use sent-in array
        int k = 0;
        Node<E> p;
        for (p = first(); p != null && k < a.length; p = succ(p)) {
            E item = p.item;
            if (item != null)
                a[k++] = (T)item;
        }
        if (p == null) {
            if (k < a.length)
                a[k] = null;
            return a;
        }

        // If won't fit, use ArrayList version
        ArrayList<E> al = new ArrayList<E>();
        for (Node<E> q = first(); q != null; q = succ(q)) {
            E item = q.item;
            if (item != null)
                al.add(item);
        }
        return al.toArray(a);
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        private Node<E> nextNode;

        private E nextItem;

        private Node<E> lastRet;

        Itr() {
            advance();
        }

        private E advance() {
            lastRet = nextNode;
            E x = nextItem;

            Node<E> pred, p;
            if (nextNode == null) {
                p = first();
                pred = null;
            } else {
                pred = nextNode;
                p = succ(nextNode);
            }

            for (;;) {
                if (p == null) {
                    nextNode = null;
                    nextItem = null;
                    return x;
                }
                E item = p.item;
                if (item != null) {
                    nextNode = p;
                    nextItem = item;
                    return x;
                } else {
                    // skip over nulls
                    Node<E> next = succ(p);
                    if (pred != null && next != null)
                        pred.casNext(p, next);
                    p = next;
                }
            }
        }

        public boolean hasNext() {
            return nextNode != null;
        }

        public E next() {
            if (nextNode == null) throw new NoSuchElementException();
            return advance();
        }

        public void remove() {
            Node<E> l = lastRet;
            if (l == null) throw new IllegalStateException();
            // rely on a future traversal to relink.
            l.item = null;
            lastRet = null;
        }
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {

        // Write out any hidden stuff
        s.defaultWriteObject();

        // Write out all elements in the proper order.
        for (Node<E> p = first(); p != null; p = succ(p)) {
            Object item = p.item;
            if (item != null)
                s.writeObject(item);
        }

        // Use trailing null as sentinel
        s.writeObject(null);
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Read in elements until trailing null sentinel found
        Node<E> h = null, t = null;
        Object item;
        while ((item = s.readObject()) != null) {
            @SuppressWarnings("unchecked")
            Node<E> newNode = new Node<E>((E) item);
            if (h == null)
                h = t = newNode;
            else {
                t.lazySetNext(newNode);
                t = newNode;
            }
        }
        if (h == null)
            h = t = new Node<E>(null);
        head = h;
        tail = t;
    }

    static final class CLQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 1 << 25;  // max batch array size;
        final ConcurrentLinkedQueue<E> queue;
        Node<E> current;    // current node; null until initialized
        int batch;          // batch size for splits
        boolean exhausted;  // true when no more nodes
        CLQSpliterator(ConcurrentLinkedQueue<E> queue) {
            this.queue = queue;
        }

        public Spliterator<E> trySplit() {
            Node<E> p;
            final ConcurrentLinkedQueue<E> q = this.queue;
            int b = batch;
            int n = (b <= 0) ? 1 : (b >= MAX_BATCH) ? MAX_BATCH : b + 1;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null) &&
                p.next != null) {
                Object[] a = new Object[n];
                int i = 0;
                do {
                    if ((a[i] = p.item) != null)
                        ++i;
                    if (p == (p = p.next))
                        p = q.first();
                } while (p != null && i < n);
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

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                exhausted = true;
                do {
                    E e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                    if (e != null)
                        action.accept(e);
                } while (p != null);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            final ConcurrentLinkedQueue<E> q = this.queue;
            if (!exhausted &&
                ((p = current) != null || (p = q.first()) != null)) {
                E e;
                do {
                    e = p.item;
                    if (p == (p = p.next))
                        p = q.first();
                } while (e == null && p != null);
                if ((current = p) == null)
                    exhausted = true;
                if (e != null) {
                    action.accept(e);
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

    @Override
    public Spliterator<E> spliterator() {
        return new CLQSpliterator<E>(this);
    }

    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, tailOffset, cmp, val);
    }

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return UNSAFE.compareAndSwapObject(this, headOffset, cmp, val);
    }

    // Unsafe mechanics

    private static final sun.misc.Unsafe UNSAFE;
    private static final long headOffset;
    private static final long tailOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentLinkedQueue.class;
            headOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("head"));
            tailOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
