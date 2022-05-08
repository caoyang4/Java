package java.util.concurrent;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * DelayQueue 是BlockingQueue接口的实现类，
 * 它根据"延时时间"来确定队列内的元素的处理优先级（即根据队列元素的“延时时间”进行排序）。
 * 另一层含义是只有那些超过“延时时间”的元素才能从队列里面被拿出来进行处理。
 *
 * DelayQueue 队列将阻止其元素对象从队列中被取出，直到达到为元素对象设置的延迟时间。
 * DelayQueue 在队列的头部存储最近过期的元素，如果队列内没有元素过期，使用poll()方法获取队列内的元素将会返回null。
 * DelayQueue 类及其迭代器实现了Collection和Iterator接口的所有可选方法，但迭代器方法iterator()不能保证以特定的顺序遍历DelayQueue的元素。
 * DelayQueue 不接收null元素，DelayQueue 只接受那些实现了java.util.concurrent.Delayed接口的对象，并将其放入队列内
 */
public class DelayQueue<E extends Delayed> extends AbstractQueue<E> implements BlockingQueue<E> {

    private final transient ReentrantLock lock = new ReentrantLock();
    // 内部基于优先级队列
    private final PriorityQueue<E> q = new PriorityQueue<E>();
    // 排队等待获取头结点的线程
    private Thread leader = null;

    private final Condition available = lock.newCondition();

    public DelayQueue() {}

    public DelayQueue(Collection<? extends E> c) {
        this.addAll(c);
    }

    public boolean add(E e) {
        return offer(e);
    }

    public boolean offer(E e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // 新增
            q.offer(e);
            // 判断插入的元素是否是第一个
            // 当插入的元素在队列头时，肯定会打断原有等待队列头的线程leader，
            // 然后在take方法中的死循环重新选出leader，等待队列头时候超时
            if (q.peek() == e) {
                // 把排队线程置为null
                leader = null;
                // 是第一个就要唤醒在available条件队列里面的线程，
                // 因为此时最短的延迟时间更新了，要唤醒让线程重新去取
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void put(E e) {
        offer(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    public E poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E first = q.peek();
            if (first == null || first.getDelay(NANOSECONDS) > 0)
                return null;
            else
                return q.poll();
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                //取队列第一个元素
                E first = q.peek();
                if (first == null)
                    //没有元素就进入等待队列
                    available.await();
                else {
                    //算出剩余到期时间
                    //注意这是的getDelay()是调用的需要自己实现的
                    long delay = first.getDelay(NANOSECONDS);
                    if (delay <= 0)
                        //<=0说明到时间了，可以取出了，调Poll
                        return q.poll();
                    // 等待期间，不持有引用
                    first = null;
                    if (leader != null)
                        // 有其他等待线程，先进入等待队列
                        available.await();
                    else {
                        Thread thisThread = Thread.currentThread();
                        // 延迟未满足，先将 leader 赋值为当前线程，避免后续线程竞争
                        leader = thisThread;
                        try {
                            //休眠剩余到期时间
                            available.awaitNanos(delay);
                        } finally {
                            if (leader == thisThread)
                                // 休眠结束，满足延迟，将 leader 置空，意味着其他线程可以竞争获取元素了
                                leader = null;
                        }
                    }
                }
            }
        } finally {
            if (leader == null && q.peek() != null)
                //唤醒下一个
                available.signal();
            lock.unlock();
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                E first = q.peek();
                if (first == null) {
                    if (nanos <= 0)
                        return null;
                    else
                        nanos = available.awaitNanos(nanos);
                } else {
                    long delay = first.getDelay(NANOSECONDS);
                    if (delay <= 0)
                        return q.poll();
                    if (nanos <= 0)
                        return null;
                    first = null; // don't retain ref while waiting
                    if (nanos < delay || leader != null)
                        nanos = available.awaitNanos(nanos);
                    else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            long timeLeft = available.awaitNanos(delay);
                            nanos -= delay - timeLeft;
                        } finally {
                            if (leader == thisThread)
                                leader = null;
                        }
                    }
                }
            }
        } finally {
            if (leader == null && q.peek() != null)
                available.signal();
            lock.unlock();
        }
    }

    public E peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.peek();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.size();
        } finally {
            lock.unlock();
        }
    }

    private E peekExpired() {
        // assert lock.isHeldByCurrentThread();
        E first = q.peek();
        return (first == null || first.getDelay(NANOSECONDS) > 0) ?
            null : first;
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = 0;
            for (E e; (e = peekExpired()) != null;) {
                c.add(e);       // In this order, in case add() throws.
                q.poll();
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
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
            int n = 0;
            for (E e; n < maxElements && (e = peekExpired()) != null;) {
                c.add(e);       // In this order, in case add() throws.
                q.poll();
                ++n;
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
            q.clear();
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.toArray();
        } finally {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.remove(o);
        } finally {
            lock.unlock();
        }
    }

    void removeEQ(Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Iterator<E> it = q.iterator(); it.hasNext(); ) {
                if (o == it.next()) {
                    it.remove();
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    private class Itr implements Iterator<E> {
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

        @SuppressWarnings("unchecked")
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

}
