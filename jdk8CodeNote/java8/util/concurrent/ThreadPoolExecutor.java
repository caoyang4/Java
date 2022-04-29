package java.util.concurrent;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class ThreadPoolExecutor extends AbstractExecutorService {
    // 记录线程池的生命周期状态和当前工作的线程
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    // 默认值29
    private static final int COUNT_BITS = Integer.SIZE - 3;
    // 允许的最大线程数 maximumPoolSize最大值也是它
    // 0001 1111 1111 1111  1111 1111 1111 1111
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits, 高3位记录状态，低29位记录线程数量
    // 线程池被创建的初始状态
    // 此状态下线程池会接受新任务并且处理队列中等待的任务
    private static final int RUNNING    = -1 << COUNT_BITS; // 111
    // 线程池处在SHUTDOWN状态时，不接收新任务，但能处理已添加的任务
    // 此状态下线程池不接受新任务，但会处理队列中等待的任务
    // 调用线程池的shutdown()接口时，线程池由RUNNING -> SHUTDOWN
    private static final int SHUTDOWN   =  0 << COUNT_BITS; // 000
    // 所有的任务已终止 ctl中任务数量是零
    // 此状态下线程池不接受新任务，也不处理既有等待任务，并且会中断既有运行中的线程
    // 当线程池在SHUTDOWN状态下，阻塞队列为空并且线程池中执行的任务也为空时，就会由 SHUTDOWN -> TIDYING。
    // 当线程池在STOP状态下，线程池中执行的任务为空时，就会由STOP -> TIDYING
    private static final int STOP       =  1 << COUNT_BITS; // 001

    private static final int TIDYING    =  2 << COUNT_BITS; // 010
    // 线程池彻底终止，就变成TERMINATED状态
    // 线程池处在TIDYING状态时，执行完terminated()之后，就会由 TIDYING -> TERMINATED
    private static final int TERMINATED =  3 << COUNT_BITS; // 011

    // Packing and unpacking ctl
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    // 线程数
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    /*
     * Bit field accessors that don't require unpacking ctl.
     * These depend on the bit layout and on workerCount being never negative.
     */

    private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    private static boolean isRunning(int c) {return c < SHUTDOWN;}

    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    private void decrementWorkerCount() {
        do {} while (! compareAndDecrementWorkerCount(ctl.get()));
    }

    // 任务阻塞队列
    private final BlockingQueue<Runnable> workQueue;

    /**
     * 用锁可以串行化interruptIdleWorkers方法，避免关闭线程池时大量线程并发中断其他线程。
     * 另外在shutdown/shutdownNow时由于需要遍历工作线程集合来检查权限，在检查完权限后会中断工作线程。
     * 加上锁也可以保证在检查权限与中断线程过程中，工作线程集合元素不变
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    private final HashSet<Worker> workers = new HashSet<Worker>();

    private final Condition termination = mainLock.newCondition();

    private int largestPoolSize;

    private long completedTaskCount;


    private volatile ThreadFactory threadFactory;

    private volatile RejectedExecutionHandler handler;

    // 线程存活时间
    private volatile long keepAliveTime;

    private volatile boolean allowCoreThreadTimeOut;

    // 核心线程数量
    private volatile int corePoolSize;

    // 最大线程数量
    private volatile int maximumPoolSize;

    // 拒绝策略执行控制器
    private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

    /* The context to be used when executing the finalizer, or null. */
    private final AccessControlContext acc;

    /**
     * 基于AQS，实现不可重入锁
     * 本质上是一个被封装起来的线程，用来运行提交到线程池里面的任务，当没有任务的时候就去队列里面 take 或者 poll 等着
     * worker 类存在的主要意义就是为了维护线程的中断状态
     */
    private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
        private static final long serialVersionUID = 6138294804551838833L;

        final Thread thread;
        Runnable firstTask;
        volatile long completedTasks;

        Worker(Runnable firstTask) {
            // 设置-1，是因为 runworker，最开始就有一个 unlock 操作
            // 在线程真正开始运行任务之前，抑制中断
            setState(-1); // inhibit interrupts until runWorker
            this.firstTask = firstTask;
            // 新建线程
            this.thread = getThreadFactory().newThread(this);
        }

        public void run() {
            runWorker(this);
        }

        // Lock methods
        // The value 0 represents the unlocked state.
        // The value 1 represents the locked state.

        /**
         * 只需要两个状态，一个是独占锁，表明正在执行任务；一个是不加锁，表明是空闲状态
         */
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        protected boolean tryAcquire(int unused) {
            // 不可重入，因为正在执行任务的线程是不应该被中断的，而ReentrantLock是可重入锁
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        // 获取锁
        public void lock()        { acquire(1); }
        // 尝试获取锁，成功返回 true，反之 false
        public boolean tryLock()  { return tryAcquire(1); }
        // 释放锁
        public void unlock()      { release(1); }
        public boolean isLocked() { return isHeldExclusively(); }

        void interruptIfStarted() {
            Thread t;
            // 不能中断刚初始化的worker，即state为 -1 的 worker
            // 正在执行的 worker 也会被中断
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }

    /*
     * Methods for setting control state
     */

    private void advanceRunState(int targetState) {
        for (;;) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
                break;
        }
    }

    /**
     * 实现线程池状态从SHUTDOWN或者STOP流转到TIDYING->TERMINATED的桥梁方法
     */
    final void tryTerminate() {
        for (;;) {
            int c = ctl.get();
            if (isRunning(c) ||  // running状态直接返回
                runStateAtLeast(c, TIDYING) || // TIDYING、TERMINATED  说明已经状态已经中断成功 无需再次中断
                (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty())) // SHUTDOWN,且任务队列不为空，返回
                return;
            /*
             * 能够进行状态流转的情况是:
             * 1. STOP状态
             * 2. SHUTDOWN并且任务队列已空。
             */

            /*
             * 这时只需要所有工作线程退出即可终止线程池。
             * 如果仍然有工作线程，则中断一个空闲的线程。
             *
             * 一旦空闲线程被终止，则会进入processWorkerExit方法，
             * 在processWorkerExit方法中即将退出的工作线程会调用tryTerminate，
             * 从而将终止线程池的动作通过这样的机制在线程间传播下去。
             */
            if (workerCountOf(c) != 0) { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // 此时，workerCount已经为0，任务队列也已为空,状态流转到TIDYING
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                    try {
                        // 调用terminated()钩子方法
                        terminated();
                    } finally {
                        // 将线程池状态拨到TERMINATED。
                        ctl.set(ctlOf(TERMINATED, 0));
                        // 唤醒所有在线程池终止条件上等待的线程
                        termination.signalAll();
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // 线程池状态流转CAS失败的话重试循环
            // else retry on failed CAS
        }
    }

    /*
     * Methods for controlling interrupts to worker threads.
     */

    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * 此方法只会被shutdownNow方法调用，用于中断所有工作线程。
     */
    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers)
                w.interruptIfStarted();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 参数中的onlyOne表示至多只中断一个工作线程。
     * 在tryTerminate方法读取到目前线程池离可以进入终止状态只剩下workCount降为0时，
     * 会调用interruptIdeleWorkers(true)。因为有可能此时其他所有线程都阻塞在任务队列上，
     * 只要中断任意一个线程，通过processWorkerExit -> tryTerminate ->interruptIdleWorkers，
     * 可以使线程中断+退出传播下去。
     */
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        /*
         * 这里加全局锁的一个很重要的目的是使这个方法串行化执行。
         * 否则在线程池关闭阶段,退出的线程会通过tryTerminate调用到此方法,
         * 并发尝试中断还未中断的线程，引发一场中断风暴。
         */
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                // 此处，也是将 worker 设计为锁不可重入的原因，防止任务执行的时候重入锁被打断
                // 工作线程在处理任务阶段是被互斥锁保护的
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                // shutdown方法，onlyOne为true，即中断所有空闲的worker
                if (onlyOne)
                    break;
            }
        } finally {
            mainLock.unlock();
        }
    }

    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    /*
     * Misc utilities, most of which are also exported to
     * ScheduledThreadPoolExecutor
     */

    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    void onShutdown() {
    }

    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(ctl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }

    /*
     * Methods for creating, running and cleaning up after workers
     */

    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);
            // 如果线程池状态至少为STOP,返回false，不接受任务。
            if (rs >= SHUTDOWN &&
                // 如果线程池状态为SHUTDOWN，并且传过来的任务不为null，或者任务队列为空，同样不接受任务。
                ! (rs == SHUTDOWN && firstTask == null && ! workQueue.isEmpty()))
                return false;

            for (;;) {
                int wc = workerCountOf(c);
                // 线程数超限
                if (wc >= CAPACITY ||
                    // 根据是否是核心线程，判断是否超过corePoolSize或者maximumPoolSize
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                // 成功新增workCount,跳出整个循环往下走
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                /*
                 * 重读总控状态,如果线程池运行状态变了，重试整个大循环。
                 * 否则说明是workCount发生了变化，cas失败，重试内层循环。
                 */
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }
        // 运行到此处时，线程池线程数已经成功+1,下面进行实质操作
        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {
            // 创建 worker，其中创建了一个线程
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                // 在执行之前,worker要先获取锁，避免在执行期间被其他的线程中断，而且因为workers是HashSet类型的，不能保证线程安全

                // 假设我们使用的是并发安全的 Set 集合，不用 mainLock。
                // 这个时候有 5 个线程都来调用 shutdown 方法，由于没有用 mainLock ，所以没有阻塞，那么每一个线程都会运行 interruptIdleWorkers。
                // 所以，就会出现第一个线程发起了中断，导致 worker ，即线程正在中断中。第二个线程又来发起中断了，于是再次对正在中断中的中断发起中断。
                // 也就是：对正在中断中的中断，发起中断。
                // 因此，这里用锁是为了避免中断风暴（interrupt storms）的风险
                mainLock.lock();
                try {
                    // 由于获取锁之前线程池状态可能发生了变化，这里需要重新读一次状态
                    int rs = runStateOf(ctl.get());
                    // 线程池状态为 RUNNING
                    if (rs < SHUTDOWN ||
                        // 线程池状态为SHUTDOWN，且task为null
                        (rs == SHUTDOWN && firstTask == null)) {
                        // 检查线程状态
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        // 向工作线程集合HashSet添加新worker,更新largestPoolSize
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                // 成功增加worker后，启动该worker线程
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            // worker线程如果没有成功启动，回滚worker集合和worker计数器的变化。
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

    // 新增工作线程失败的处理
    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null)
                // 从worker集合删除失败的worker
                workers.remove(w);
            // workCount减1
            decrementWorkerCount();
            // 尝试终止线程池
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        /*
         * 异常的情况下，需要在本方法里给workCount减1。
         */
        if (completedAbruptly) // If abrupt, then workerCount wasn't adjusted
            decrementWorkerCount();

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        // 加锁保护completedTaskCount，workers
        try {
            // 累加completedTaskCount,从工作线程集合移除自己
            completedTaskCount += w.completedTasks;
            workers.remove(w);
        } finally {
            mainLock.unlock();
        }
        // 由于workCount减1，需要调用tryTerminate方法
        tryTerminate();

        int c = ctl.get();
        // 只要线程池还没达到STOP状态，任务队列中的任务仍然是需要处理的
        if (runStateLessThan(c, STOP)) {
            if (!completedAbruptly) {
                /*
                 * 确定在RUNNING或SHUTDOWN状态下最少需要的工作线程数。
                 *
                 * 默认情况下，核心线程不受限制时影响，
                 * 在这种情况下核心线程数量应当是稳定的。
                 * 否则允许线程池中无线程。
                 */
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && ! workQueue.isEmpty())
                    // 队列不为空，至少需要保证一个线程存活
                    min = 1;
                if (workerCountOf(c) >= min)
                    return; // replacement not needed
            }
            // 异常退出或者需要补偿一个线程的情况下，加一个空任务工作线程
            addWorker(null, false);
        }
    }

    /**
     * 工作线程从任务队列中拿取任务的核心方法。
     * 根据配置决定采用阻塞或是时限获取。
     * 在以下几种情况会返回null，从而接下来线程会退出(runWorker方法循环结束):
     * 1. 当前工作线程数超过了maximumPoolSize(由于maximumPoolSize可以动态调整，这是可能的)。
     * 2. 线程池状态为STOP (因为STOP状态不处理任务队列中的任务了)。
     * 3. 线程池状态为SHUTDOWN,任务队列为空 (因为SHUTDOWN状态仍然需要处理等待中任务)。
     * 4. 根据线程池参数状态以及线程是否空闲超过keepAliveTime决定是否退出当前工作线程。
     */
    private Runnable getTask() {
        boolean timedOut = false; // Did the last poll() time out?

        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // 线程池状态是Stop，或者线程池是SHUTDOWN，且是空队列
            // SHUTDOWN状态仍然需要处理等待中任务
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                // 任务-1
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            /*
             * allowCoreThreadTimeOut是用于设置核心线程是否受keepAliveTime影响。
             * 在allowCoreThreadTimeOut为true或者工作线程数>corePoolSize情况下，当前工作线程受keepAliveTime影响。
             */
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            /*
             * 1. 工作线程数>maximumPoolSize,当前工作线程需要退出。
             * 2. timed && timedOut == true说明当前线程受keepAliveTime影响且上次获取任务超时。
             *    这种情况下只要当前线程不是最后一个工作线程或者任务队列为空，则可以退出。
             *
             *    换句话说就是，如果队列不为空，则当前线程不能是最后一个工作线程，否则退出了就没线程处理任务了。
             */
            if ((wc > maximumPoolSize || (timed && timedOut))
                && (wc > 1 || workQueue.isEmpty())) {
                // 设置ctl的workCount减1, CAS失败则需要重试
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
            }

            try {
                // 根据timed变量的值决定是时限获取或是阻塞获取任务队列中的任务
                Runnable r = timed ?
                    workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                    workQueue.take();
                if (r != null)
                    return r;
                // workQueue.take是不会返回null的，因此说明poll超时
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        // 便于GC
        w.firstTask = null;
        // 置互斥锁状态为0，此时可以被中断
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {
            // 初始任务（首次）或者从阻塞阻塞队列里拿一个（后续）
            while (task != null || (task = getTask()) != null) {
                /*
                 * 获取互斥锁。
                 * 在持有互斥锁时，调用线程池shutdown方法不会中断该线程。
                 * 但是shutdownNow方法无视互斥锁，会中断所有线程。
                 */
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt

                // 如果线程池至少处于STOP阶段，当前线程未中断，则中断当前线程,否则清除线程中断位
                if ((runStateAtLeast(ctl.get(), STOP) ||

                     (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) &&
                    // 没有中断，清除线程中断位
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
                    // 调用由子类实现的前置处理钩子
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
                        // 真正的执行任务
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
                        // 调用由子类实现的后置处理钩子
                        afterExecute(task, thrown);
                    }
                } finally {
                    // 清空task, 计数器+1, 释放互斥锁
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            /*
             * 处理工作线程退出。
             * 上面主循环中的前置处理、任务调用、后置处理都是可能会抛出异常的。
             */
            processWorkerExit(w, completedAbruptly);
        }
    }

    // Public constructors and methods

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ? null : AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * 任务添加入口
     *
     */
    public void execute(Runnable command) {
        if (command == null) throw new NullPointerException();
        /*
         * 分类讨论:
         * 1. 如果当前线程数小于核心线程数，则会开启一个新线程来执行提交的任务。
         *
         * 2. 尝试向任务队列中添加任务。这时需要再次检查方法开始到当前时刻这段间隙,
         *    线程池是否已经关闭，以及线程池中有没有工作线程
         *    如果线程池已经关闭，需要在任务队列中移除先前提交的任务。
         *    如果没有工作线程，则需要添加一个空任务工作线程用于执行提交的任务。
         *
         * 3. 如果无法向阻塞队列中添加任务，则尝试创建一个新的线程执行任务。
         *    如果失败，回调拒绝策略处理任务。
         */
        int c = ctl.get();
        // 线程数量小于核心线程数量
        if (workerCountOf(c) < corePoolSize) {
            // 创建核心线程数量，并将任务包装
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        // 超过核心线程数
        // 线程池是Running状态，成功将任务添加进阻塞队列
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            // 线程池不是Running状态，不接收新任务，将刚刚添加队列的任务删除
            if (! isRunning(recheck) && remove(command))
                reject(command);
            //工作线程数是0的时候新增一个非核心线程处理任务，工作线程从队列获取任务
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        // 添加队列失败
        else if (!addWorker(command, false))
            reject(command);
    }

    /**
     * shutdown方法关闭线程池是有序优雅的，线程池进入SHUTDOWN状态后不会接受新任务，但是任务队列中已有的任务会继续处理。
     * shutdown方法会中断所有未处理任务的空闲线程
     */
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // 检查权限
            checkShutdownAccess();
            // 状态切换到SHUTDOWN
            advanceRunState(SHUTDOWN);
            // 中断所有空闲线程，或者说在任务队列上阻塞的线程
            interruptIdleWorkers();
            // 钩子方法
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        // 尝试终止线程池(状态流转至TERMINATED)
        tryTerminate();
    }

    /**
     * shutdownNow方法关闭线程池相比shutdown就暴力了一点，会中断所有线程，哪怕线程正在执行任务。
     * 线程池进入STOP状态后，不接受新的任务，也不会处理任务队列中已有的任务。
     * 但需要注意的是，即便shutdownNow即便会中断正在执行任务的线程，不代表你的任务一定会挂：
     *    如果提交的任务里面的代码没有对线程中断敏感的逻辑的话，线程中断也不会发生什么
     * @return
     */
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            // 状态切换到STOP
            advanceRunState(STOP);
            // 与SHUTDOWN不同的是，直接中断所有线程
            interruptWorkers();
            // 将任务队列中的任务收集到tasks
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return ! isRunning(ctl.get());
    }

    public boolean isTerminating() {
        int c = ctl.get();
        return ! isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }


    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
                if (runStateAtLeast(ctl.get(), TERMINATED))
                    return true;
                if (nanos <= 0)
                    return false;
                // 线程池关闭之前，其他线程阻塞等待
                // //如果时间没有被耗尽，那么会返回剩下的需要等待的时间
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    protected void finalize() {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null || acc == null) {
            shutdown();
        } else {
            PrivilegedAction<Void> pa = () -> { shutdown(); return null; };
            AccessController.doPrivileged(pa, acc);
        }
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null)
            throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null)
            throw new NullPointerException();
        this.handler = handler;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0)
            throw new IllegalArgumentException();
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(ctl.get()) > corePoolSize)
            interruptIdleWorkers();
        else if (delta > 0) {
            // We don't really know how many new threads are "needed".
            // As a heuristic, prestart enough new workers (up to new
            // core size) to handle the current number of tasks in
            // queue, but stop if queue becomes empty while doing so.
            int k = Math.min(delta, workQueue.size());
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty())
                    break;
            }
        }
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public boolean prestartCoreThread() {
        return workerCountOf(ctl.get()) < corePoolSize &&
            addWorker(null, true);
    }

    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize)
            addWorker(null, true);
        else if (wc == 0)
            addWorker(null, false);
    }

    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, true))
            ++n;
        return n;
    }

    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0)
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value)
                interruptIdleWorkers();
        }
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
            throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(ctl.get()) > maximumPoolSize)
            interruptIdleWorkers();
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0)
            throw new IllegalArgumentException();
        if (time == 0 && allowsCoreThreadTimeOut())
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0)
            interruptIdleWorkers();
    }

    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    /* User-level queue utilities */

    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    it.remove();
            }
        } catch (ConcurrentModificationException fallThrough) {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray())
                if (r instanceof Future<?> && ((Future<?>)r).isCancelled())
                    q.remove(r);
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }

    /* Statistics */

    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            // Remove rare and surprising possibility of
            // isTerminated() && getPoolSize() > 0
            return runStateAtLeast(ctl.get(), TIDYING) ? 0
                : workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers)
                if (w.isLocked())
                    ++n;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }

    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked())
                    ++n;
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers)
                n += w.completedTasks;
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    public String toString() {
        long ncompleted;
        int nworkers, nactive;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            ncompleted = completedTaskCount;
            nactive = 0;
            nworkers = workers.size();
            for (Worker w : workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked())
                    ++nactive;
            }
        } finally {
            mainLock.unlock();
        }
        int c = ctl.get();
        String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" :
                     (runStateAtLeast(c, TERMINATED) ? "Terminated" :
                      "Shutting down"));
        return super.toString() +
            "[" + rs +
            ", pool size = " + nworkers +
            ", active threads = " + nactive +
            ", queued tasks = " + workQueue.size() +
            ", completed tasks = " + ncompleted +
            "]";
    }

    /* Extension hooks */

    protected void beforeExecute(Thread t, Runnable r) { }

    protected void afterExecute(Runnable r, Throwable t) { }

    protected void terminated() { }

    /* Predefined RejectedExecutionHandlers */

    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public CallerRunsPolicy() { }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    public static class AbortPolicy implements RejectedExecutionHandler {
        public AbortPolicy() { }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }
    }

    public static class DiscardPolicy implements RejectedExecutionHandler {
        public DiscardPolicy() { }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        public DiscardOldestPolicy() { }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
}
