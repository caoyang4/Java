package java.util.concurrent;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 *
 * @code
 * public class CustomScheduledExecutor extends ScheduledThreadPoolExecutor {
 *   static class CustomTask<V> implements RunnableScheduledFuture<V> { ... }
 *   protected <V> RunnableScheduledFuture<V> decorateTask(
 *                Runnable r, RunnableScheduledFuture<V> task) {
 *       return new CustomTask<V>(r, task);
 *   }
 *   protected <V> RunnableScheduledFuture<V> decorateTask(
 *                Callable<V> c, RunnableScheduledFuture<V> task) {
 *       return new CustomTask<V>(c, task);
 *   }
 *   // ... add constructors, etc.
 * }}
 */

/**
 * ScheduleExecutorService实则是Timer的进化版，主要改进了Timer单线程方面的弊端，改进方式自然是线程池，继承自ThreadPoolExecutor
 * 线程池中的任务队列用的new DelayedWorkQueue()，而DelayedWorkQueue是ScheduledThreadPoolExecutor的内部类
 *
 * 它是一个可以把提交的任务延迟执行或者周期执行的线程池。比起java.util.Timer类，它更加灵活。
 * 延迟任务提交到线程池后开始执行，但具体何时执行则不知道，延迟任务是根据先进先出（FIFO）的提交顺序来执行的。
 * 当提交的任务在运行之前被取消时，执行将被禁止。
 * 默认情况下，这样一个被取消的任务不会自动从工作队列中删除，直到它的延迟过期为止。
 * 虽然这可以进行进一步的检查和监视，但也可能导致取消的任务无限制地保留。
 * 为了避免这种情况，将setRemoveOnCancelPolicy设置为true，这将导致任务在取消时立即从工作队列中删除。
 * 通过scheduleAtFixedRate或scheduleWithFixedDelay调度的任务的连续执行不会重叠。
 * 虽然这个类继承自ThreadPoolExecutor，但是一些继承的调优方法对它并不有用。
 * 特别是，由于它使用corePoolSize线程和无界队列(队列最大为Integer.MAX_VALUE)充当固定大小的池，
 * 所以对maximumPoolSize的调整没有任何有用的效果。
 *
 */
public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {

    // 线程池停止后,周期任务取消执行则为false,默认为false,不取消则为ture
    private volatile boolean continueExistingPeriodicTasksAfterShutdown;
    // 线程池停止后,取消非周期任务则为false,不取消非周期任务则为true,默认为true
    private volatile boolean executeExistingDelayedTasksAfterShutdown = true;
    // 取消的任务是否移出队列,默认为false不移出
    private volatile boolean removeOnCancel = false;
    // 任务进入队列的序列号
    private static final AtomicLong sequencer = new AtomicLong();

    final long now() {
        return System.nanoTime();
    }

    /**
     * ScheduledFutureTask实现Runnable接口并继承FutureTask类，表明该类是一个被包装的任务，
     * 同时该类实现Delayed接口和Comparable< Delayed>接口，表明该任务具有延迟性和优先级
     *
     * ScheduledFutureTask类是对Runnable类的一个包装;
     * Runnable任务转变成ScheduledFutureTask任务,使得任务具有周期性,周期任务放入DelayedWorkQueue具有延迟性;
     * ScheduledFutureTask类封装time属性和period属性,分别代表延迟时间和周期时间;
     * ScheduledFutureTask类有三个构造函数，可分别构造周期性任务和非周期性任务;
     * ScheduledFutureTask类重写FutureTask类的run()方法,进而执行周期性任务;
     * ScheduledFutureTask类实现compareTo(T)方法,使得队列中的延迟性任务根据延迟时间的长短排出优先级;
     */
    private class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

        // FIFO队列的序列号
        private final long sequenceNumber;
        // 以毫秒为单位的相对于任务创建时刻的等待时间，即延迟时间
        private long time;
        // 以纳秒为单位的周期时间，正数表明fixed-rate执行，负数表明delay-rate执行，0表明非重复
        private final long period;
        // 当前任务
        RunnableScheduledFuture<V> outerTask = this;
        // 进入延迟队列的索引值，它便于取消任务
        int heapIndex;

        /**
         * 通过类的属性和构造函数可知,ScheduledFutureTask类使得Runnable任务可以延迟执行,
         * 甚至设置周期执行;同时记录每个Runnable任务进入延迟队列的序列号.
         */
        // 创建延迟时间为ns的非重复任务，返回结果为result
        ScheduledFutureTask(Runnable r, V result, long ns) {
            super(r, result);
            this.time = ns;
            // 0表示非重复
            this.period = 0;
            // 当前任务的序列号
            this.sequenceNumber = sequencer.getAndIncrement();
        }
        // 创建延迟时间为ns,周期时间为period,返回结果为result的任务
        ScheduledFutureTask(Runnable r, V result, long ns, long period) {
            super(r, result);
            this.time = ns;
            this.period = period;
            this.sequenceNumber = sequencer.getAndIncrement();
        }
        // 创建延迟时间为ns的非重复任务
        ScheduledFutureTask(Callable<V> callable, long ns) {
            super(callable);
            this.time = ns;
            this.period = 0;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(time - now(), NANOSECONDS);
        }

        /**
         * 核心方法,在周期任务插入优先队列的时候会根据该方法判断队列的插入位置
         *
         * 当前任务与参数任务(siftUp时是父节点,siftDown时是子节点)比较time属性,即任务的延迟时间;
         * 如果是siftUp,若插入任务延迟时间大于父节点任务延迟时间,则返回1,表明节点不交换,即任务插入当前位置;
         * 否则与父节点任务交换位置并继续比较父节点的父节点;
         * 如果是siftDown,若插入任务延迟时间小于子节点任务延迟时间,则返回-1,表明节点不交换,
         * 即任务放置在当前父节点位置;否则与自己诶单任务交换位置并继续比较子节点的子节点;
         */
        public int compareTo(Delayed other) {
            // 如果与自己比较则返回0
            if (other == this) // compare zero if same object
                return 0;
            // 任务为ScheduledFutureTask类型
            if (other instanceof ScheduledFutureTask) {
                ScheduledFutureTask<?> x = (ScheduledFutureTask<?>)other;
                // 当前任务的延迟时间与比较任务的延迟时间之差
                long diff = time - x.time;
                if (diff < 0)
                    return -1;
                else if (diff > 0)
                    return 1;
                // 如果等于0,则比较任务的序列号,当前任务序列号小则返回-1,当前序列号大则返回1
                else if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }
            // 任务不为ScheduledFutureTask类型,则直接比较两者的延迟时间
            long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
            return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
        }

        public boolean isPeriodic() {
            return period != 0;
        }

        private void setNextRunTime() {
            long p = period;
            if (p > 0)
                // 若周期时间为正数，则把周期时间与延迟时间相加作为任务下次执行的时间
                time += p;
            else
                // 若为负数,则忽略延迟时间,以当前时间为基准加上周期时间作为下次任务执行时间
                time = triggerTime(-p);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            if (cancelled && removeOnCancel && heapIndex >= 0)
                remove(this);
            return cancelled;
        }

        /**
         * 非周期任务,则采用futureTask类的run()方法,不存储优先队列;
         * 周期任务,首先确定任务的延迟时间,然后把延迟任务插入优先队列
         */
        public void run() {
            boolean periodic = isPeriodic();
            // 属性period不等于0返回true,表明周期任务;否则返回false,表明非周期任务
            if (!canRunInCurrentRunState(periodic))
                // 终止线程并移除任务
                cancel(false);
            // 非周期任务执行FutureTask的run()方法
            else if (!periodic)
                ScheduledFutureTask.super.run();
            // 周期任务,新建任务且可执行
            else if (ScheduledFutureTask.super.runAndReset()) {
                // 根据period值设置该任务的time值,如果period为正数,则直接与time值相加，
                // 如果是负数，则去掉符号后与系统当前时间相加
                setNextRunTime();
                // 当前任务加入队列
                reExecutePeriodic(outerTask);
            }
        }
    }

    boolean canRunInCurrentRunState(boolean periodic) {
        return isRunningOrShutdown(periodic ?
                                   // 默认为false,表示shutDown状态下取消周期性任务.
                                   continueExistingPeriodicTasksAfterShutdown :
                                   // 默认为true,表示shutDown状态下不会取消非周期性任务
                                   executeExistingDelayedTasksAfterShutdown);
    }

    private void delayedExecute(RunnableScheduledFuture<?> task) {
        // 线程池状态为shutdown,则执行拒绝策略拒绝任务
        if (isShutdown())
            reject(task);
        else {
            // 线程状态正常,则把任务放入优先队列
            super.getQueue().add(task);
            if (isShutdown() &&
                !canRunInCurrentRunState(task.isPeriodic()) &&
                remove(task))
                // shutdown状态下,周期任务会默认移除队列
                task.cancel(false);
            else
                // 如果池内线程数小于核心线程数,则新建一个线程
                ensurePrestart();
        }
    }

    /**
     * 判断当前状态是否可以插入任务;
     * 任务插入到优先队列;
     * 创建新的线程；
     */
    // 把周期任务插入优先队列的过程.
    void reExecutePeriodic(RunnableScheduledFuture<?> task) {
        // 当前状态可以运行线程
        if (canRunInCurrentRunState(true)) {
            // task任务放入delayedWorkQueue队列中
            super.getQueue().add(task);
            if (!canRunInCurrentRunState(true)
                // 当前状态(shutdown)不可以运行线程，删除任务
                && remove(task))
                task.cancel(false);
            else
                ensurePrestart();
        }
    }

    @Override
    void onShutdown() {
        BlockingQueue<Runnable> q = super.getQueue();
        boolean keepDelayed =
            getExecuteExistingDelayedTasksAfterShutdownPolicy();
        boolean keepPeriodic =
            getContinueExistingPeriodicTasksAfterShutdownPolicy();
        if (!keepDelayed && !keepPeriodic) {
            for (Object e : q.toArray())
                if (e instanceof RunnableScheduledFuture<?>)
                    ((RunnableScheduledFuture<?>) e).cancel(false);
            q.clear();
        }
        else {
            // Traverse snapshot to avoid iterator exceptions
            for (Object e : q.toArray()) {
                if (e instanceof RunnableScheduledFuture) {
                    RunnableScheduledFuture<?> t =
                        (RunnableScheduledFuture<?>)e;
                    if ((t.isPeriodic() ? !keepPeriodic : !keepDelayed) ||
                        t.isCancelled()) { // also remove if already cancelled
                        if (q.remove(t))
                            t.cancel(false);
                    }
                }
            }
        }
        tryTerminate();
    }

    /**
     * 修改或替换用于执行的任务，可覆盖用于管理内部任务的具体类;默认实现只返回给定的任务
     */
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return task;
    }

    protected <V> RunnableScheduledFuture<V> decorateTask(
        Callable<V> callable, RunnableScheduledFuture<V> task) {
        return task;
    }

    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue());
    }

    public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory);
    }

    public ScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), handler);
    }

    /**
     * 为什么最大线程数设置为Integer.MAX_VALUE?
     *   因为延迟队列内用数组存放任务,数组初始长度为16,但数组长度会随着任务数的增加而动态扩容,直到数组长度为Integer.MAX_VALUE;
     *   既然队列能存放Integer.MAX_VALUE个任务,又因为任务是延迟任务,因此保证任务不被抛弃,最多需要Integer.MAX_VALUE个线程
     *
     * 为什么空闲线程的超时时间设置为0？
     *   空闲线程超时时间都为0,表示池内不存在空闲线程,
     *   ScheduledThreadPoolExecutor线程池会把池内的某一个线程定义为leader线程,
     *   该leader线程用于等待队列的根节点直到获取并运行任务,而其他线程则会阻塞等待;
     *   阻塞等待的线程等待leader线程释放唤醒的信号,等待队列中的某个线程会被升级为leader线程,其他线程继续等待.
     *   那么，这里等待的线程都为空闲线程,为了避免过多的线程浪费资源,所以ScheduledThreadPool线程池内更多的存活的是核心线程.
     */
    public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory, handler);
    }

    private long triggerTime(long delay, TimeUnit unit) {
        return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
    }

    long triggerTime(long delay) {
        return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
    }

    // 保证队列内任务的延迟时间都在Long.MAX_VALUE范围内
    private long overflowFree(long delay) {
        // 获取队列的头部节点,即优先级最高的任务
        Delayed head = (Delayed) super.getQueue().peek();
        if (head != null) {
            long headDelay = head.getDelay(NANOSECONDS);
            // 若任务时间已到但还没有被处理且delay和headDelay相加溢出
            if (headDelay < 0 && (delay - headDelay < 0))
                // delay取差值
                delay = Long.MAX_VALUE + headDelay;
        }
        return delay;
    }

    /**
     * @param command 执行任务
     * @param delay 延迟时间
     *
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<?> t = decorateTask(command, new ScheduledFutureTask<Void>(command, null, triggerTime(delay, unit)));
        delayedExecute(t);
        return t;
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if (callable == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<V> t = decorateTask(callable, new ScheduledFutureTask<V>(callable, triggerTime(delay, unit)));
        delayedExecute(t);
        return t;
    }

    /**
     * ScheduleWithFixedDelay与ScheduleAtFixedRate的区别在于Runnable方法的执行方式;
     * 前者是方法执行结束之后,延迟一段时间之后再执行下一次,表明前后两次执行关系紧密;
     * 后者时不管前一次方法执行有没有结束,在固定的时间后都会再次重复该方法;
     * 当然,由于两者的Runnable任务都是延迟任务,因此任务都会加入到优先队列中等候.
     * 如果使用ScheduleWithFixedDelay()方法,核心线程数尽量取小一点,这样可以避免空闲线程.
     */


    /**
     * 该方法创建一个周期性任务,且设置任务的首次执行时间,即延迟时间;
     * command:  需要执行的周期任务;
     * initialDelay:  初始化延迟时间;根据该参数计算出任务的首次执行时间;
     * period:  周期时间;任务周期化执行的时间;根据该参数计算出任务的后续执行时间;
     * unit:    时间单位;
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (period <= 0)
            throw new IllegalArgumentException();
        ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit), unit.toNanos(period));
        RunnableScheduledFuture<Void> t = decorateTask(command, sft);
        sft.outerTask = t;
        delayedExecute(t);
        return t;
    }

    /**
     * 创建并执行一个周期性的Runnable方法,该方法会在给定的初始化延迟时间之后执行;
     * Runnable方法执行结束之后，在给定的delay值(延迟时间)后再次执行;
     * 该周期性操作会在Runnable方法执行出现异常时停止,或者线程池终止时停止.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (delay <= 0)
            throw new IllegalArgumentException();
        ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit), unit.toNanos(-delay));
        RunnableScheduledFuture<Void> t = decorateTask(command, sft);
        sft.outerTask = t;
        delayedExecute(t);
        return t;
    }

    /**
     * execute(Runnable)和submit(Runnable)方法都是调用schedule()方法.
     */
    public void execute(Runnable command) {
        // 非延迟
        schedule(command, 0, NANOSECONDS);
    }

    // Override AbstractExecutorService methods

    public Future<?> submit(Runnable task) {
        return schedule(task, 0, NANOSECONDS);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return schedule(Executors.callable(task, result), 0, NANOSECONDS);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return schedule(task, 0, NANOSECONDS);
    }

    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
        continueExistingPeriodicTasksAfterShutdown = value;
        if (!value && isShutdown())
            onShutdown();
    }

    public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
        return continueExistingPeriodicTasksAfterShutdown;
    }

    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
        executeExistingDelayedTasksAfterShutdown = value;
        if (!value && isShutdown())
            onShutdown();
    }

    public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
        return executeExistingDelayedTasksAfterShutdown;
    }

    public void setRemoveOnCancelPolicy(boolean value) {
        removeOnCancel = value;
    }

    public boolean getRemoveOnCancelPolicy() {
        return removeOnCancel;
    }

    public void shutdown() {
        super.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    public BlockingQueue<Runnable> getQueue() {
        return super.getQueue();
    }

    /**
     * 该队列是定制的优先级队列，是无界队列，只能用来存储ScheduledFutureTask任务。
     * 堆是实现优先级队列的最佳选择，而该队列正好是基于堆数据结构的实现
     * 基于ReentrantLock和Condition
     *
     * 使用最小堆实现，最近要到达时间的节点放在堆顶，每个节点都会附带到期时间，依次作为堆调整的依据
     */
    static class DelayedWorkQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {

        // 数组的初始容量为16 设置为16的原因跟hashmap中数组容量为16的原因一样
        private static final int INITIAL_CAPACITY = 16;
        // 用于存储ScheduledFutureTask任务的数组
        private RunnableScheduledFuture<?>[] queue = new RunnableScheduledFuture<?>[INITIAL_CAPACITY];
        private final ReentrantLock lock = new ReentrantLock();
        // 当前队列中任务数,即队列长度
        private int size = 0;
        // leader线程用于等待队列头部任务,
        private Thread leader = null;
        // 当线程成为leader时,通知其他线程等待
        private final Condition available = lock.newCondition();

        private void setIndex(RunnableScheduledFuture<?> f, int idx) {
            if (f instanceof ScheduledFutureTask)
                ((ScheduledFutureTask)f).heapIndex = idx;
        }

        /**
         * 新增任务后重排
         */
        private void siftUp(int k, RunnableScheduledFuture<?> key) {
            // 当k为根节点时结束循环
            while (k > 0) {
                // 获取k的父节点索引,相当于(k-1)/2
                int parent = (k - 1) >>> 1;
                RunnableScheduledFuture<?> e = queue[parent];
                // 判断key任务与父节点任务time属性的大小,即延迟时间
                if (key.compareTo(e) >= 0)
                    // 父节点任务延迟时间小于key任务延迟时间,则退出循环
                    break;
                // 否则交换父节点parent与节点k的任务
                queue[k] = e;
                setIndex(e, k);
                // 更新k的值 比较其与父父节点的大小
                k = parent;
            }
            queue[k] = key;
            setIndex(key, k);
        }

        /**
         * 移除元素后重排序
         */
        private void siftDown(int k, RunnableScheduledFuture<?> key) {
            // 取队列当前深度的一半 相当于size / 2
            int half = size >>> 1;
            // 索引k(初值为0)的值大于half时 退出循环
            while (k < half) {
                int child = (k << 1) + 1;
                // 获取左节点的任务
                RunnableScheduledFuture<?> c = queue[child];
                // 获取右节点的索引
                int right = child + 1;
                // 如果右节点在范围内 且 左节点大于右节点,
                if (right < size && c.compareTo(queue[right]) > 0)
                    // 更新child的值为右节点索引值 且更新c为右节点的任务
                    c = queue[child = right];
                // 如果任务key小于任务c 则退出循环(最小堆)
                if (key.compareTo(c) <= 0)
                    break;
                // 否则把任务c放到k上(较小的任务放到父节点上)
                queue[k] = c;
                setIndex(c, k);
                k = child;
            }
            queue[k] = key;
            setIndex(key, k);
        }

        private void grow() {
            int oldCapacity = queue.length;
            // 扩容 1.5 倍
            int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
            if (newCapacity < 0) // overflow
                newCapacity = Integer.MAX_VALUE;
            queue = Arrays.copyOf(queue, newCapacity);
        }

        private int indexOf(Object x) {
            if (x != null) {
                if (x instanceof ScheduledFutureTask) {
                    int i = ((ScheduledFutureTask) x).heapIndex;
                    // Sanity check; x could conceivably be a
                    // ScheduledFutureTask from some other pool.
                    if (i >= 0 && i < size && queue[i] == x)
                        return i;
                } else {
                    for (int i = 0; i < size; i++)
                        if (x.equals(queue[i]))
                            return i;
                }
            }
            return -1;
        }

        public boolean contains(Object x) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return indexOf(x) != -1;
            } finally {
                lock.unlock();
            }
        }

        /**
         * 删除任务
         */
        public boolean remove(Object x) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = indexOf(x);
                if (i < 0)
                    return false;
                // 设置删除任务的heapIndex为-1
                setIndex(queue[i], -1);
                int s = --size;
                // 获取队列末尾的节点任务
                RunnableScheduledFuture<?> replacement = queue[s];
                queue[s] = null;
                // 如果删除的任务节点不是末尾的节点,则重排序
                if (s != i) {
                    siftDown(i, replacement);
                    if (queue[i] == replacement)
                        siftUp(i, replacement);
                }
                return true;
            } finally {
                lock.unlock();
            }
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

        public boolean isEmpty() {return size() == 0;}

        public int remainingCapacity() {return Integer.MAX_VALUE;}

        public RunnableScheduledFuture<?> peek() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return queue[0];
            } finally {
                lock.unlock();
            }
        }

        public boolean offer(Runnable x) {
            if (x == null)
                throw new NullPointerException();
            // 只能存放RunnableScheduledFuture任务
            RunnableScheduledFuture<?> e = (RunnableScheduledFuture<?>)x;
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                // 当前队列实际深度,即队列中任务个数
                int i = size;
                if (i >= queue.length)
                    // 如果任务数已经超过数组长度,则扩容为原来的1.5倍
                    grow();
                size = i + 1;
                // 如果是空队列 新增任务插入到数组头部;
                if (i == 0) {
                    queue[0] = e;
                    setIndex(e, 0);
                } else {
                    // 如果不是空队列 则调用siftUp()插入任务
                    siftUp(i, e);
                }
                // 如果作为首个任务插入到数组头部
                if (queue[0] == e) {
                    // 置空当前leader线程
                    leader = null;
                    // 唤醒一个等待的线程 使其成为leader线程
                    available.signal();
                }
            } finally {
                lock.unlock();
            }
            return true;
        }

        public void put(Runnable e) {offer(e);}
        public boolean add(Runnable e) {return offer(e);}
        public boolean offer(Runnable e, long timeout, TimeUnit unit) {return offer(e);}

        private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> f) {
            int s = --size;
            // 获取队列最后一个任务
            RunnableScheduledFuture<?> x = queue[s];
            queue[s] = null;
            // 如果s已经根节点则直接返回，否则堆重排序
            if (s != 0)
                siftDown(0, x);
            // 取出来的任务 设置其堆索引为-1
            setIndex(f, -1);
            return f;
        }

        public RunnableScheduledFuture<?> poll() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                // 获取队列根节点,即延迟时间最小,优先级最高的任务
                RunnableScheduledFuture<?> first = queue[0];
                // 如果根节点为null 或者节点延迟还没到 则返回null
                if (first == null || first.getDelay(NANOSECONDS) > 0)
                    return null;
                else
                    // 获取根节点元素后，需要对队列重新排序
                    return finishPoll(first);
            } finally {
                lock.unlock();
            }
        }

        /**
         * 等待获取根节点
         */
        public RunnableScheduledFuture<?> take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            // 中断获取锁
            lock.lockInterruptibly();
            try {
                for (;;) {
                    // 获取根节点任务
                    RunnableScheduledFuture<?> first = queue[0];
                    // 如果队列为空，则通知其他线程等待
                    if (first == null)
                        // 线程阻塞
                        available.await();
                    else {
                        // 获取根节点任务等待时间与系统时间的差值
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0)
                            // 如果等待时间已经到，则返回根节点任务并重排序队列
                            return finishPoll(first);
                        // 如果等待时间还没有到,则继续等待且不拥有任务的引用，让线程等待
                        first = null; // don't retain ref while waiting
                        // 如果此时等待根节点的leader线程不为空则通知其他线程继续等待
                        if (leader != null)
                            available.await();
                        else {
                            // 如果此时leader线程为空,则把当前线程置为leader
                            Thread thisThread = Thread.currentThread();
                            leader = thisThread;
                            try {
                                // 当前线程等待延迟的时间
                                available.awaitNanos(delay);
                            } finally {
                                // 延迟时间已到 则把当前线程变成非leader线程
                                // 当前线程继续用于执行for循环的逻辑
                                if (leader == thisThread)
                                    leader = null;
                            }
                        }
                    }
                }
            } finally {
                // 如果leader为null 则唤醒一个线程成为leader
                if (leader == null && queue[0] != null)
                    available.signal();
                lock.unlock();
            }
        }

        /**
         * 超时等待获取根节点
         */
        public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit)
            throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (;;) {
                    RunnableScheduledFuture<?> first = queue[0];
                    if (first == null) {
                        if (nanos <= 0)
                            return null;
                        else
                            nanos = available.awaitNanos(nanos);
                    } else {
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0)
                            return finishPoll(first);
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
                if (leader == null && queue[0] != null)
                    available.signal();
                lock.unlock();
            }
        }

        public void clear() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                for (int i = 0; i < size; i++) {
                    RunnableScheduledFuture<?> t = queue[i];
                    if (t != null) {
                        queue[i] = null;
                        setIndex(t, -1);
                    }
                }
                size = 0;
            } finally {
                lock.unlock();
            }
        }

        private RunnableScheduledFuture<?> peekExpired() {
            // assert lock.isHeldByCurrentThread();
            RunnableScheduledFuture<?> first = queue[0];
            return (first == null || first.getDelay(NANOSECONDS) > 0) ?
                null : first;
        }

        public int drainTo(Collection<? super Runnable> c) {
            if (c == null)
                throw new NullPointerException();
            if (c == this)
                throw new IllegalArgumentException();
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                RunnableScheduledFuture<?> first;
                int n = 0;
                while ((first = peekExpired()) != null) {
                    c.add(first);   // In this order, in case add() throws.
                    finishPoll(first);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

        public int drainTo(Collection<? super Runnable> c, int maxElements) {
            if (c == null)
                throw new NullPointerException();
            if (c == this)
                throw new IllegalArgumentException();
            if (maxElements <= 0)
                return 0;
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                RunnableScheduledFuture<?> first;
                int n = 0;
                while (n < maxElements && (first = peekExpired()) != null) {
                    c.add(first);   // In this order, in case add() throws.
                    finishPoll(first);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

        public Object[] toArray() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return Arrays.copyOf(queue, size, Object[].class);
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (a.length < size)
                    return (T[]) Arrays.copyOf(queue, size, a.getClass());
                System.arraycopy(queue, 0, a, 0, size);
                if (a.length > size)
                    a[size] = null;
                return a;
            } finally {
                lock.unlock();
            }
        }

        public Iterator<Runnable> iterator() {
            return new Itr(Arrays.copyOf(queue, size));
        }

        private class Itr implements Iterator<Runnable> {
            final RunnableScheduledFuture<?>[] array;
            int cursor = 0;     // index of next element to return
            int lastRet = -1;   // index of last element, or -1 if no such

            Itr(RunnableScheduledFuture<?>[] array) {
                this.array = array;
            }

            public boolean hasNext() {
                return cursor < array.length;
            }

            public Runnable next() {
                if (cursor >= array.length)
                    throw new NoSuchElementException();
                lastRet = cursor;
                return array[cursor++];
            }

            public void remove() {
                if (lastRet < 0)
                    throw new IllegalStateException();
                DelayedWorkQueue.this.remove(array[lastRet]);
                lastRet = -1;
            }
        }
    }
}
