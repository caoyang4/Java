package java.util.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.Consumer;
import sun.misc.SharedSecrets;

/**
 *
 * @code
 * class FIFOEntry<E extends Comparable<? super E>>
 *     implements Comparable<FIFOEntry<E>> {
 *   static final AtomicLong seq = new AtomicLong(0);
 *   final long seqNum;
 *   final E entry;
 *   public FIFOEntry(E entry) {
 *     seqNum = seq.getAndIncrement();
 *     this.entry = entry;
 *   }
 *   public E getEntry() { return entry; }
 *   public int compareTo(FIFOEntry<E> other) {
 *     int res = entry.compareTo(other.entry);
 *     if (res == 0 && other.entry != this.entry)
 *       res = (seqNum < other.seqNum ? -1 : 1);
 *     return res;
 *   }
 * }}
 */

/**
 * PriorityBlockingQueue是BlockingQueue接口的实现类，它是一种优先级阻塞队列，
 * 每次出队都返回优先级最高或最低的元素，其内部是用平衡二叉树的最大/最小堆实现的，入队和出队操作，都需要维护这个堆。
 * 这里的优先级指的是元素类必须实现Comparable接口，然后用compareTo()方法比较元素的优先级大小，当然也可指定自定义的比较器comparator。
 */
@SuppressWarnings("unchecked")
public class PriorityBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {
    private static final long serialVersionUID = 5595510919245408276L;
    // 队列默认容量为11
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    // 队列最大容量
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    // 底层数组实现
    private transient Object[] queue;

    private transient int size;
    // 自定义比较器
    private transient Comparator<? super E> comparator;
    // 操作元素数组的互斥锁
    private final ReentrantLock lock;
    // 数组非空条件变量
    private final Condition notEmpty;
    // 数组扩容操作的自璇锁，1表示正在扩容，0表示没有在扩容
    private transient volatile int allocationSpinLock;
    // 普通的优先级队列，只有序列化/反序列化时会用到它，入队出队没有用到，是为了和之前的JDK版本兼容
    private PriorityQueue<E> q;

    public PriorityBlockingQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    public PriorityBlockingQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    public PriorityBlockingQueue(int initialCapacity, Comparator<? super E> comparator) {
        if (initialCapacity < 1)
            throw new IllegalArgumentException();
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.comparator = comparator;
        // 存放元素的数组初始化
        this.queue = new Object[initialCapacity];
    }

    public PriorityBlockingQueue(Collection<? extends E> c) {
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        boolean heapify = true; // true if not known to be in heap order
        boolean screen = true;  // true if must screen for nulls
        if (c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            heapify = false;
        }
        else if (c instanceof PriorityBlockingQueue<?>) {
            PriorityBlockingQueue<? extends E> pq =
                (PriorityBlockingQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            screen = false;
            if (pq.getClass() == PriorityBlockingQueue.class) // exact match
                heapify = false;
        }
        Object[] a = c.toArray();
        int n = a.length;
        if (c.getClass() != java.util.ArrayList.class)
            a = Arrays.copyOf(a, n, Object[].class);
        if (screen && (n == 1 || this.comparator != null)) {
            for (int i = 0; i < n; ++i)
                if (a[i] == null)
                    throw new NullPointerException();
        }
        this.queue = a;
        this.size = n;
        // 堆化
        if (heapify)
            heapify();
    }
    // 数组扩容
    private void tryGrow(Object[] array, int oldCap) {
        // 先释放操作数组的互斥锁，去尝试获取扩容的自璇锁
        lock.unlock(); // must release and then re-acquire main lock
        Object[] newArray = null;
        // 尝试获取扩容的锁
        if (allocationSpinLock == 0 &&
            UNSAFE.compareAndSwapInt(this, allocationSpinLockOffset, 0, 1)) {
            try {
                // 计算扩容后的新容量
                // 扩容前容量小于64时，扩容为原来2倍+2，大于等于64时，扩容为原来的1.5倍
                int newCap = oldCap + ((oldCap < 64) ?
                                       (oldCap + 2) : // grow faster if small
                                       (oldCap >> 1));
                // 新容量超出最大容量时，则取最大容量
                if (newCap - MAX_ARRAY_SIZE > 0) {    // possible overflow
                    // 旧容量加1仍然溢出时，抛内存溢出异常
                    int minCap = oldCap + 1;
                    if (minCap < 0 || minCap > MAX_ARRAY_SIZE)
                        throw new OutOfMemoryError();
                    // 置为最大容量
                    newCap = MAX_ARRAY_SIZE;
                }
                // 扩容成功，初始化新数组
                if (newCap > oldCap && queue == array)
                    newArray = new Object[newCap];
            } finally {
                // 释放扩容操作的自璇锁
                allocationSpinLock = 0;
            }
        }
        // 其他线程抢到了扩容锁并正在扩容时，当前线程则让出CPU调度权
        if (newArray == null) // back off if another thread is allocating
            Thread.yield();
        // 获取操作数组的互斥锁，用于数组拷贝迁移
        lock.lock();
        // 扩容操作成功时，将旧数组元素拷贝到扩容后的新数组
        if (newArray != null && queue == array) {
            queue = newArray;
            System.arraycopy(array, 0, newArray, 0, oldCap);
        }
    }

    private E dequeue() {
        int n = size - 1;
        // 队列为空时返回null
        if (n < 0)
            return null;
        else {
            Object[] array = queue;
            // 保存队列头部元素，即优先级最大或最小的元素，也就是要出队的元素
            E result = (E) array[0];
            // 记录队尾元素
            E x = (E) array[n];
            // 将队列尾部位置元素置为null
            array[n] = null;
            Comparator<? super E> cmp = comparator;
            // 重新调整堆，使得移除头部元素后仍然保持堆的性质。
            if (cmp == null)
                siftDownComparable(0, x, array, n);
            else
                siftDownUsingComparator(0, x, array, n, cmp);
            size = n;
            return result;
        }
    }

    private static <T> void siftUpComparable(int k, T x, Object[] array) {
        Comparable<? super T> key = (Comparable<? super T>) x;
        // 数组0号位置即为堆的根节点，从底层往根节点遍历
        while (k > 0) {
            // 获取当前结点的父结点
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            // 待插入元素比父结点元素优先级高时，k即为我们要找的待插入元素的位置，退出循环
            if (key.compareTo((T) e) >= 0)
                break;
            // 否则将父结点元素值复制到当前位置k
            array[k] = e;
            // k从父结点位置再往上遍历，找合适的位置
            k = parent;
        }
        array[k] = key;
    }
    // 它与siftUpComparable()逻辑类似，只是比较优先级时，采用用户传入的自定义比较器
    private static <T> void siftUpUsingComparator(int k, T x, Object[] array, Comparator<? super T> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            if (cmp.compare(x, (T) e) >= 0)
                break;
            array[k] = e;
            k = parent;
        }
        array[k] = x;
    }

    private static <T> void siftDownComparable(int k, T x, Object[] array, int n) {
        if (n > 0) {
            Comparable<? super T> key = (Comparable<? super T>)x;
            int half = n >>> 1;           // loop while a non-leaf
            // 由于移除了根节点，要从左右子树中找到最小值来当新的根节点
            // 从根节点开始往叶子节点遍历
            while (k < half) {
                // 先获取左孩子的值
                int child = (k << 1) + 1; // assume left child is least
                Object c = array[child];
                // 再获取右孩子的值
                int right = child + 1;
                if (right < n &&
                    // 右孩子比左孩子小时，c变更为右孩子的值
                    ((Comparable<? super T>) c).compareTo((T) array[right]) > 0)
                    c = array[child = right];
                // 尾节点的值比c小时，找到目标根节点，结束循环
                if (key.compareTo((T) c) <= 0)
                    break;
                // 否则将父结点置为左右孩子较小的那个值
                array[k] = c;
                // k赋值为左右孩子较小的那个结点，继续往下找
                k = child;
            }
            // 将尾结点移动到合适位置k，此时最小的节点在遍历的过程中也已经移动的堆的顶部了
            array[k] = key;
        }
    }

    private static <T> void siftDownUsingComparator(int k, T x, Object[] array,
                                                    int n,
                                                    Comparator<? super T> cmp) {
        if (n > 0) {
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && cmp.compare((T) c, (T) array[right]) > 0)
                    c = array[child = right];
                if (cmp.compare(x, (T) c) <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = x;
        }
    }

    private void heapify() {
        Object[] array = queue;
        int n = size;
        int half = (n >>> 1) - 1;
        Comparator<? super E> cmp = comparator;
        if (cmp == null) {
            for (int i = half; i >= 0; i--)
                siftDownComparable(i, (E) array[i], array, n);
        }
        else {
            for (int i = half; i >= 0; i--)
                siftDownUsingComparator(i, (E) array[i], array, n, cmp);
        }
    }
    // 入队，自下而上的堆化
    public boolean add(E e) {
        return offer(e);
    }

    public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        int n, cap;
        Object[] array;
        // 队列当前长度>=队列容量时，进行扩容
        while ((n = size) >= (cap = (array = queue).length))
            tryGrow(array, cap);
        try {
            Comparator<? super E> cmp = comparator;
            // 未指定比较器时，则使用元素默认或自实现的compareTo()来计算插入元素的位置，即维护堆的过程。
            if (cmp == null)
                siftUpComparable(n, e, array);
            else
                siftUpUsingComparator(n, e, array, cmp);
            size = n + 1;
            // 唤醒某个等待在notEmpty条件的线程
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
        return true;
    }
    // 不需要阻塞
    public void put(E e) {
        offer(e); // never need to block
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e); // never need to block
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 非阻塞，队列为空时返回null
            return dequeue();
        } finally {
            lock.unlock();
        }
    }
    // 出队，自上而下的堆化
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        // 中断式获取锁
        lock.lockInterruptibly();
        E result;
        try {
            // 队列元素为空时，阻塞等待
            while ( (result = dequeue()) == null)
                notEmpty.await();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        E result;
        try {
            while ( (result = dequeue()) == null && nanos > 0)
                nanos = notEmpty.awaitNanos(nanos);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return (size == 0) ? null : (E) queue[0];
        } finally {
            lock.unlock();
        }
    }

    public Comparator<? super E> comparator() {
        return comparator;
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    private int indexOf(Object o) {
        if (o != null) {
            Object[] array = queue;
            int n = size;
            for (int i = 0; i < n; i++)
                if (o.equals(array[i]))
                    return i;
        }
        return -1;
    }

    private void removeAt(int i) {
        Object[] array = queue;
        int n = size - 1;
        if (n == i) // removed last element
            array[i] = null;
        else {
            E moved = (E) array[n];
            array[n] = null;
            Comparator<? super E> cmp = comparator;
            if (cmp == null)
                siftDownComparable(i, moved, array, n);
            else
                siftDownUsingComparator(i, moved, array, n, cmp);
            if (array[i] == moved) {
                if (cmp == null)
                    siftUpComparable(i, moved, array);
                else
                    siftUpUsingComparator(i, moved, array, cmp);
            }
        }
        size = n;
    }

    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i == -1)
                return false;
            removeAt(i);
            return true;
        } finally {
            lock.unlock();
        }
    }

    void removeEQ(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = queue;
            for (int i = 0, n = size; i < n; i++) {
                if (o == array[i]) {
                    removeAt(i);
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return indexOf(o) != -1;
        } finally {
            lock.unlock();
        }
    }

    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return Arrays.copyOf(queue, size);
        } finally {
            lock.unlock();
        }
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (n == 0)
                return "[]";
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < n; ++i) {
                Object e = queue[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (i != n - 1)
                    sb.append(',').append(' ');
            }
            return sb.append(']').toString();
        } finally {
            lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        if (maxElements <= 0)
            return 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = Math.min(size, maxElements);
            for (int i = 0; i < n; i++) {
                c.add((E) queue[0]); // In this order, in case add() throws.
                dequeue();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = queue;
            int n = size;
            size = 0;
            for (int i = 0; i < n; i++)
                array[i] = null;
        } finally {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (a.length < n)
                // Make a new array of a's runtime type, but my contents:
                return (T[]) Arrays.copyOf(queue, size, a.getClass());
            System.arraycopy(queue, 0, a, 0, n);
            if (a.length > n)
                a[n] = null;
            return a;
        } finally {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    final class Itr implements Iterator<E> {
        final Object[] array; // Array of all elements
        int cursor;           // index of next element to return
        int lastRet;          // index of last element, or -1 if no such

        Itr(Object[] array) {
            lastRet = -1;
            this.array = array;
        }

        public boolean hasNext() {
            return cursor < array.length;
        }

        public E next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return (E)array[cursor++];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            removeEQ(array[lastRet]);
            lastRet = -1;
        }
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        lock.lock();
        try {
            // avoid zero capacity argument
            q = new PriorityQueue<E>(Math.max(size, 1), comparator);
            q.addAll(this);
            s.defaultWriteObject();
        } finally {
            q = null;
            lock.unlock();
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        try {
            s.defaultReadObject();
            int sz = q.size();
            SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, sz);
            this.queue = new Object[sz];
            comparator = q.comparator();
            addAll(q);
        } finally {
            q = null;
        }
    }

    // Similar to Collections.ArraySnapshotSpliterator but avoids
    // commitment to toArray until needed
    static final class PBQSpliterator<E> implements Spliterator<E> {
        final PriorityBlockingQueue<E> queue;
        Object[] array;
        int index;
        int fence;

        PBQSpliterator(PriorityBlockingQueue<E> queue, Object[] array,
                       int index, int fence) {
            this.queue = queue;
            this.array = array;
            this.index = index;
            this.fence = fence;
        }

        final int getFence() {
            int hi;
            if ((hi = fence) < 0)
                hi = fence = (array = queue.toArray()).length;
            return hi;
        }

        public Spliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null :
                new PBQSpliterator<E>(queue, array, lo, index = mid);
        }

        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action) {
            Object[] a; int i, hi; // hoist accesses and checks from loop
            if (action == null)
                throw new NullPointerException();
            if ((a = array) == null)
                fence = (a = queue.toArray()).length;
            if ((hi = fence) <= a.length &&
                (i = index) >= 0 && i < (index = hi)) {
                do { action.accept((E)a[i]); } while (++i < hi);
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            if (getFence() > index && index >= 0) {
                @SuppressWarnings("unchecked") E e = (E) array[index++];
                action.accept(e);
                return true;
            }
            return false;
        }

        public long estimateSize() { return (long)(getFence() - index); }

        public int characteristics() {
            return Spliterator.NONNULL | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    public Spliterator<E> spliterator() {
        return new PBQSpliterator<E>(this, null, 0, -1);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long allocationSpinLockOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = PriorityBlockingQueue.class;
            allocationSpinLockOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("allocationSpinLock"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
