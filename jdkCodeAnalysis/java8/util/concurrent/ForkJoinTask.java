package java.util.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.reflect.Constructor;

/**
 * 较 ThreadPoolExecutor 的单个 TaskQueue 的形式，ForkJoinPool 是多个 TaskQueue的形式
 *
 * 提交的任务主要有两种：
 *   有外部直接提交的（submission task）
 *   也有任务自己 fork 出来的（worker task）
 *   为了进一步区分这两种 task，Doug Lea 就设计一个简单的路由规则：
 *     将 submission task 放到 WorkQueue 数组的「偶数」下标中
 *     将 worker task 放在 WorkQueue 的「奇数」下标中，并且只有奇数下标才有线程( worker )与之相对
 */
public abstract class ForkJoinTask<V> implements Future<V>, Serializable {

    /*
     * The methods of this class are more-or-less layered into
     * (1) basic status maintenance
     * (2) execution and awaiting completion
     * (3) user-level methods that additionally report results.
     * This is sometimes hard to see because this file orders exported
     * methods in a way that flows well in javadocs.
     */

    volatile int status; // accessed directly by pool and workers
    static final int DONE_MASK   = 0xf0000000;  // mask out non-completion bits
    // 任务正常完成 -1
    static final int NORMAL      = 0xf0000000;  // must be negative
    // 任务被取消  -2
    static final int CANCELLED   = 0xc0000000;  // must be < NORMAL
    // 任务异常终止 -3
    static final int EXCEPTIONAL = 0x80000000;  // must be < CANCELLED
    // 某个线程在等待当前任务执行完成，需要在任务结束时唤醒等待的线程  1
    static final int SIGNAL      = 0x00010000;  // must be >= 1 << 16
    static final int SMASK       = 0x0000ffff;  // short bits for tags

    private int setCompletion(int completion) {
        for (int s;;) {
            if ((s = status) < 0)
                return s;
            if (U.compareAndSwapInt(this, STATUS, s, s | completion)) {
                if ((s >>> 16) != 0)
                    synchronized (this) { notifyAll(); }
                return completion;
            }
        }
    }

    // 执行任务，doExec方法的返回值取决于exec方法，
    // 如果exec返回true，则doExec返回值小于0
    // 如果返回false，则doExec返回值大于等于0
    final int doExec() {
        int s; boolean completed;
        if ((s = status) >= 0) {
            try {
                // ForkJoinTask中的抽象方法，RecursiveTask 和 RecursiveAction 都重写了它
                completed = exec();
            } catch (Throwable rex) {
                return setExceptionalCompletion(rex);
            }
            if (completed)
                // 正常结束
                s = setCompletion(NORMAL);
        }
        return s;
    }

    final void internalWait(long timeout) {
        int s;
        if ((s = status) >= 0 && // force completer to issue notify
            U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
            synchronized (this) {
                if (status >= 0)
                    try { wait(timeout); } catch (InterruptedException ie) { }
                else
                    notifyAll();
            }
        }
    }

    // 阻塞普通线程等待任务执行完成
    private int externalAwaitDone() {
        int s = ((this instanceof CountedCompleter) ? // try helping
                // 如果是CountedCompleter，则通过externalHelpComplete方法阻塞当前线程等待任务完成
                ForkJoinPool.common.externalHelpComplete((CountedCompleter<?>)this, 0) :
                // 如果是普通的ForkJoinTask，则通过tryExternalUnpush尝试将其从任务队列中pop出来，
                // 如果该任务位于任务队列顶端，则pop成功并返回true
                // pop成功后执行doExec方法，即通过当前线程完成任务
                 ForkJoinPool.common.tryExternalUnpush(this) ? doExec() : 0);
        if (s >= 0 && (s = status) >= 0) {
            boolean interrupted = false;
            do {
                // 修改status，加上SIGNAL标识，表示有线程等待了
                if (U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
                    synchronized (this) {
                        // 再次校验状态
                        if (status >= 0) {
                            try {
                                // 0表示无期限等待，直到被唤醒
                                // 任务执行完成，可通过setCompletion方法唤醒等待的线程
                                wait(0L);
                            } catch (InterruptedException ie) {
                                interrupted = true;
                            }
                        }
                        else
                            // 任务已执行完成，则唤醒所有等待的线程
                            notifyAll();
                    }
                }
            } while ((s = status) >= 0);
            if (interrupted)
                Thread.currentThread().interrupt();
        }
        return s;
    }

    private int externalInterruptibleAwaitDone() throws InterruptedException {
        int s;
        if (Thread.interrupted())
            throw new InterruptedException();
        if ((s = status) >= 0 &&
            (s = ((this instanceof CountedCompleter) ?
                  ForkJoinPool.common.externalHelpComplete(
                      (CountedCompleter<?>)this, 0) :
                  ForkJoinPool.common.tryExternalUnpush(this) ? doExec() :
                  0)) >= 0) {
            while ((s = status) >= 0) {
                if (U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
                    synchronized (this) {
                        if (status >= 0)
                            wait(0L);
                        else
                            notifyAll();
                    }
                }
            }
        }
        return s;
    }

    private int doJoin() {
        int s; Thread t; ForkJoinWorkerThread wt; ForkJoinPool.WorkQueue w;

        return (s = status) < 0 ? s :
            // 如果是 ForkJoinWorkerThread Worker
            ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            // 取出了任务，就去执行它，并返回结果
            (w = (wt = (ForkJoinWorkerThread)t).workQueue).ryUnpush(this) && (s = doExec()) < 0 ? s :
            // 也有可能别的线程把这个任务偷走了，那就执行内部等待方法
            wt.pool.awaitJoin(w, this, 0L) :
            // 如果不是 ForkJoinWorkerThread，执行外部等待方法
            externalAwaitDone();

        //awaitJoin 和 externalAwaitDone 都用到了 Helper（帮助） 和 Compensating（补偿） 两种策略
    }

    private int doInvoke() {
        int s; Thread t; ForkJoinWorkerThread wt;
        // 直接调用doExec方法执行任务，如果执行完成，则直接返回
        return (s = doExec()) < 0 ? s :
            // 如果doExec的结果大于等于0，说明未完成，典型的如CountedCompleter的子类应用
            ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            // 如果是ForkJoinWorkerThread，通过awaitJoin方法等待任务执行完成
            (wt = (ForkJoinWorkerThread)t).pool.awaitJoin(wt.workQueue, this, 0L) :
            // 普通线程，通过externalAwaitDone等待任务执行完成
            externalAwaitDone();
    }

    // Exception table support
    // 保存ExceptionNode的数组，单个数组元素对应一个ExceptionNode链表
    private static final ExceptionNode[] exceptionTable;
    private static final ReentrantLock exceptionTableLock;
    // 保存被清除掉的弱引用的队列
    private static final ReferenceQueue<Object> exceptionTableRefQueue;

    private static final int EXCEPTION_MAP_CAPACITY = 32;

    // 单向链表，继承弱引用
    static final class ExceptionNode extends WeakReference<ForkJoinTask<?>> {
        final Throwable ex;
        ExceptionNode next;
        final long thrower;  // use id not ref to avoid weak cycles
        final int hashCode;  // store task hashCode before weak ref disappears
        ExceptionNode(ForkJoinTask<?> task, Throwable ex, ExceptionNode next) {
            super(task, exceptionTableRefQueue);
            this.ex = ex;
            this.next = next;
            this.thrower = Thread.currentThread().getId();
            this.hashCode = System.identityHashCode(task);
        }
    }

    final int recordExceptionalCompletion(Throwable ex) {
        int s;
        if ((s = status) >= 0) {
            int h = System.identityHashCode(this);
            final ReentrantLock lock = exceptionTableLock;
            lock.lock();
            try {
                expungeStaleExceptions();
                ExceptionNode[] t = exceptionTable;
                int i = h & (t.length - 1);
                for (ExceptionNode e = t[i]; ; e = e.next) {
                    if (e == null) {
                        t[i] = new ExceptionNode(this, ex, t[i]);
                        break;
                    }
                    if (e.get() == this) // already present
                        break;
                }
            } finally {
                lock.unlock();
            }
            s = setCompletion(EXCEPTIONAL);
        }
        return s;
    }

    private int setExceptionalCompletion(Throwable ex) {
        int s = recordExceptionalCompletion(ex);
        if ((s & DONE_MASK) == EXCEPTIONAL)
            internalPropagateException(ex);
        return s;
    }

    void internalPropagateException(Throwable ex) {
    }

    static final void cancelIgnoringExceptions(ForkJoinTask<?> t) {
        if (t != null && t.status >= 0) {
            try {
                t.cancel(false);
            } catch (Throwable ignore) {
            }
        }
    }

    private void clearExceptionalCompletion() {
        int h = System.identityHashCode(this);
        final ReentrantLock lock = exceptionTableLock;
        lock.lock();
        try {
            ExceptionNode[] t = exceptionTable;
            int i = h & (t.length - 1);
            ExceptionNode e = t[i];
            ExceptionNode pred = null;
            while (e != null) {
                ExceptionNode next = e.next;
                if (e.get() == this) {
                    if (pred == null)
                        t[i] = next;
                    else
                        pred.next = next;
                    break;
                }
                pred = e;
                e = next;
            }
            expungeStaleExceptions();
            status = 0;
        } finally {
            lock.unlock();
        }
    }

    private Throwable getThrowableException() {
        if ((status & DONE_MASK) != EXCEPTIONAL)
            return null;
        int h = System.identityHashCode(this);
        ExceptionNode e;
        final ReentrantLock lock = exceptionTableLock;
        lock.lock();
        try {
            expungeStaleExceptions();
            ExceptionNode[] t = exceptionTable;
            e = t[h & (t.length - 1)];
            while (e != null && e.get() != this)
                e = e.next;
        } finally {
            lock.unlock();
        }
        Throwable ex;
        if (e == null || (ex = e.ex) == null)
            return null;
        if (e.thrower != Thread.currentThread().getId()) {
            Class<? extends Throwable> ec = ex.getClass();
            try {
                Constructor<?> noArgCtor = null;
                Constructor<?>[] cs = ec.getConstructors();// public ctors only
                for (int i = 0; i < cs.length; ++i) {
                    Constructor<?> c = cs[i];
                    Class<?>[] ps = c.getParameterTypes();
                    if (ps.length == 0)
                        noArgCtor = c;
                    else if (ps.length == 1 && ps[0] == Throwable.class) {
                        Throwable wx = (Throwable)c.newInstance(ex);
                        return (wx == null) ? ex : wx;
                    }
                }
                if (noArgCtor != null) {
                    Throwable wx = (Throwable)(noArgCtor.newInstance());
                    if (wx != null) {
                        wx.initCause(ex);
                        return wx;
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return ex;
    }

    private static void expungeStaleExceptions() {
        for (Object x; (x = exceptionTableRefQueue.poll()) != null;) {
            if (x instanceof ExceptionNode) {
                int hashCode = ((ExceptionNode)x).hashCode;
                ExceptionNode[] t = exceptionTable;
                int i = hashCode & (t.length - 1);
                ExceptionNode e = t[i];
                ExceptionNode pred = null;
                while (e != null) {
                    ExceptionNode next = e.next;
                    if (e == x) {
                        if (pred == null)
                            t[i] = next;
                        else
                            pred.next = next;
                        break;
                    }
                    pred = e;
                    e = next;
                }
            }
        }
    }

    static final void helpExpungeStaleExceptions() {
        final ReentrantLock lock = exceptionTableLock;
        if (lock.tryLock()) {
            try {
                expungeStaleExceptions();
            } finally {
                lock.unlock();
            }
        }
    }

    static void rethrow(Throwable ex) {
        if (ex != null)
            ForkJoinTask.<RuntimeException>uncheckedThrow(ex);
    }

    @SuppressWarnings("unchecked") static <T extends Throwable>
        void uncheckedThrow(Throwable t) throws T {
        throw (T)t; // rely on vacuous cast
    }

    private void reportException(int s) {
        if (s == CANCELLED)
            throw new CancellationException();
        if (s == EXCEPTIONAL)
            rethrow(getThrowableException());
    }

    // public methods

    /**
     * fork方法并不会创建一个新线程来执行任务，只是将任务提交到任务队列中而已
     * 如果当前线程是 ForkJoinWorkerThread 类型，也就是说已经通过上文注册的 Worker，
     * 那么直接调用 push 方法将 task 放到当前线程拥有的 WorkQueue 中，
     * 否则就再调用 externalPush
     */
    public final ForkJoinTask<V> fork() {
        Thread t;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
            ((ForkJoinWorkerThread)t).workQueue.push(this);
        else
            ForkJoinPool.common.externalPush(this);
        return this;
    }

    /**
     * join方法等待任务执行完成并返回结果，如果出现异常则报告异常
     * join 的核心调用在 doJoin
     */
    public final V join() {
        int s;
        if ((s = doJoin() & DONE_MASK) != NORMAL)
            reportException(s);
        return getRawResult();
    }

    /**
     * invoke会立即执行当前任务，如果doExec方法返回值大于等于0说明还有其他的子任务未完成，
     * 则等待其他子任务执行完成，典型的应用场景就是CountedCompleter，
     * RecursiveAction和RecursiveTask通常doExec返回值小于0，
     * 会在compute方法即执行exec方法时等待所有的子任务执行完成；quietlyInvoke和invoke 都是基于doInvoke实现，
     * 区别在于前者不关心执行的结果，不会抛出异常
     */
    public final V invoke() {
        int s;
        if ((s = doInvoke() & DONE_MASK) != NORMAL)
            reportException(s);
        return getRawResult();
    }

    public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2) {
        int s1, s2;
        t2.fork();
        if ((s1 = t1.doInvoke() & DONE_MASK) != NORMAL)
            t1.reportException(s1);
        if ((s2 = t2.doJoin() & DONE_MASK) != NORMAL)
            t2.reportException(s2);
    }

    public static void invokeAll(ForkJoinTask<?>... tasks) {
        Throwable ex = null;
        int last = tasks.length - 1;
        for (int i = last; i >= 0; --i) {
            ForkJoinTask<?> t = tasks[i];
            if (t == null) {
                if (ex == null)
                    ex = new NullPointerException();
            }
            else if (i != 0)
                t.fork();
            else if (t.doInvoke() < NORMAL && ex == null)
                ex = t.getException();
        }
        for (int i = 1; i <= last; ++i) {
            ForkJoinTask<?> t = tasks[i];
            if (t != null) {
                if (ex != null)
                    t.cancel(false);
                else if (t.doJoin() < NORMAL)
                    ex = t.getException();
            }
        }
        if (ex != null)
            rethrow(ex);
    }

    public static <T extends ForkJoinTask<?>> Collection<T> invokeAll(Collection<T> tasks) {
        if (!(tasks instanceof RandomAccess) || !(tasks instanceof List<?>)) {
            invokeAll(tasks.toArray(new ForkJoinTask<?>[tasks.size()]));
            return tasks;
        }
        @SuppressWarnings("unchecked")
        List<? extends ForkJoinTask<?>> ts = (List<? extends ForkJoinTask<?>>) tasks;
        Throwable ex = null;
        int last = ts.size() - 1;
        for (int i = last; i >= 0; --i) {
            ForkJoinTask<?> t = ts.get(i);
            if (t == null) {
                if (ex == null)
                    ex = new NullPointerException();
            }
            else if (i != 0)
                t.fork();
            else if (t.doInvoke() < NORMAL && ex == null)
                ex = t.getException();
        }
        for (int i = 1; i <= last; ++i) {
            ForkJoinTask<?> t = ts.get(i);
            if (t != null) {
                if (ex != null)
                    t.cancel(false);
                else if (t.doJoin() < NORMAL)
                    ex = t.getException();
            }
        }
        if (ex != null)
            rethrow(ex);
        return tasks;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return (setCompletion(CANCELLED) & DONE_MASK) == CANCELLED;
    }

    public final boolean isDone() {
        return status < 0;
    }

    public final boolean isCancelled() {
        return (status & DONE_MASK) == CANCELLED;
    }

    public final boolean isCompletedAbnormally() {
        return status < NORMAL;
    }

    public final boolean isCompletedNormally() {
        return (status & DONE_MASK) == NORMAL;
    }

    public final Throwable getException() {
        int s = status & DONE_MASK;
        return ((s >= NORMAL)    ? null :
                (s == CANCELLED) ? new CancellationException() :
                getThrowableException());
    }

    public void completeExceptionally(Throwable ex) {
        setExceptionalCompletion((ex instanceof RuntimeException) ||
                                 (ex instanceof Error) ? ex :
                                 new RuntimeException(ex));
    }

    public void complete(V value) {
        try {
            setRawResult(value);
        } catch (Throwable rex) {
            setExceptionalCompletion(rex);
            return;
        }
        setCompletion(NORMAL);
    }

    public final void quietlyComplete() {
        setCompletion(NORMAL);
    }

    /**
     *  get方法是阻塞当前线程并等待任务执行完成，其效果和实现跟join方法基本一致，
     *  最大的区别在于如果线程等待的过程中被中断了，get方法会抛出异常InterruptedException，而join方法不会抛出异常
     */
    public final V get() throws InterruptedException, ExecutionException {
        // 等待任务结束，返回任务结束的状态
        int s = (Thread.currentThread() instanceof ForkJoinWorkerThread) ?
            doJoin() : externalInterruptibleAwaitDone();
        Throwable ex;
        // 任务被取消了，重新抛出异常
        if ((s &= DONE_MASK) == CANCELLED)
            throw new CancellationException();
        // 任务执行过程抛出异常，将该异常重新抛出
        if (s == EXCEPTIONAL && (ex = getThrowableException()) != null)
            throw new ExecutionException(ex);
        // 取任务执行结果，抽象方法，由子类实现
        return getRawResult();
    }

    public final V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        int s;
        long nanos = unit.toNanos(timeout);
        // 被中断抛出异常
        if (Thread.interrupted())
            throw new InterruptedException();
        if ((s = status) >= 0 && nanos > 0L) {
            // 获取等待的终止时间
            long d = System.nanoTime() + nanos;
            long deadline = (d == 0L) ? 1L : d; // avoid 0
            Thread t = Thread.currentThread();
            if (t instanceof ForkJoinWorkerThread) {
                // 如果是ForkJoinWorkerThread，通过awaitJoin方法等待任务执行完成
                ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
                s = wt.pool.awaitJoin(wt.workQueue, this, deadline);
            }
            // 如果是普通线程
            else if ((s = ((this instanceof CountedCompleter) ?
                           // 如果是CountedCompleter，则通过externalHelpComplete等待其执行完成
                           ForkJoinPool.common.externalHelpComplete((CountedCompleter<?>)this, 0) :
                           // 如果是普通的ForkJoinTask，尝试将其从任务队列中pop出来并执行
                           ForkJoinPool.common.tryExternalUnpush(this) ?
                           doExec() : 0)) >= 0) {
                long ns, ms; // measure in nanosecs, but wait in millisecs
                while ((s = status) >= 0 &&
                       (ns = deadline - System.nanoTime()) > 0L) {
                    if ((ms = TimeUnit.NANOSECONDS.toMillis(ns)) > 0L &&
                        U.compareAndSwapInt(this, STATUS, s, s | SIGNAL)) {
                        synchronized (this) {
                            // 如果status大于0，即 SIGNAL 状态，则wait，反之则notifyAll
                            if (status >= 0)
                                // 阻塞当前线程指定时间，如果被中断则抛出异常
                                wait(ms); // OK to throw InterruptedException
                            else
                                notifyAll();
                        }
                    }
                }
            }
        }
        if (s >= 0)
            s = status;
        if ((s &= DONE_MASK) != NORMAL) {
            Throwable ex;
            if (s == CANCELLED)
                throw new CancellationException();
            if (s != EXCEPTIONAL)
                throw new TimeoutException();
            if ((ex = getThrowableException()) != null)
                throw new ExecutionException(ex);
        }
        return getRawResult();
    }
    // 只是等待任务执行完成，不报告异常
    public final void quietlyJoin() {
        doJoin();
    }

    public final void quietlyInvoke() {
        doInvoke();
    }

    public static void helpQuiesce() {
        Thread t;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread)t;
            wt.pool.helpQuiescePool(wt.workQueue);
        }
        else
            ForkJoinPool.quiesceCommonPool();
    }

    public void reinitialize() {
        if ((status & DONE_MASK) == EXCEPTIONAL)
            clearExceptionalCompletion();
        else
            status = 0;
    }

    public static ForkJoinPool getPool() {
        Thread t = Thread.currentThread();
        return (t instanceof ForkJoinWorkerThread) ?
            ((ForkJoinWorkerThread) t).pool : null;
    }

    public static boolean inForkJoinPool() {
        return Thread.currentThread() instanceof ForkJoinWorkerThread;
    }

    public boolean tryUnfork() {
        Thread t;
        return (((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
                ((ForkJoinWorkerThread)t).workQueue.tryUnpush(this) :
                ForkJoinPool.common.tryExternalUnpush(this));
    }

    public static int getQueuedTaskCount() {
        Thread t; ForkJoinPool.WorkQueue q;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
            q = ((ForkJoinWorkerThread)t).workQueue;
        else
            q = ForkJoinPool.commonSubmitterQueue();
        return (q == null) ? 0 : q.queueSize();
    }

    public static int getSurplusQueuedTaskCount() {
        return ForkJoinPool.getSurplusQueuedTaskCount();
    }

    // Extension methods

    public abstract V getRawResult();

    protected abstract void setRawResult(V value);

    protected abstract boolean exec();

    protected static ForkJoinTask<?> peekNextLocalTask() {
        Thread t; ForkJoinPool.WorkQueue q;
        if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
            q = ((ForkJoinWorkerThread)t).workQueue;
        else
            q = ForkJoinPool.commonSubmitterQueue();
        return (q == null) ? null : q.peek();
    }

    protected static ForkJoinTask<?> pollNextLocalTask() {
        Thread t;
        return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            ((ForkJoinWorkerThread)t).workQueue.nextLocalTask() :
            null;
    }

    protected static ForkJoinTask<?> pollTask() {
        Thread t; ForkJoinWorkerThread wt;
        return ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread) ?
            (wt = (ForkJoinWorkerThread)t).pool.nextTaskFor(wt.workQueue) :
            null;
    }

    // tag operations

    public final short getForkJoinTaskTag() {
        return (short)status;
    }

    public final short setForkJoinTaskTag(short tag) {
        for (int s;;) {
            if (U.compareAndSwapInt(this, STATUS, s = status, (s & ~SMASK) | (tag & SMASK)))
                return (short)s;
        }
    }

    public final boolean compareAndSetForkJoinTaskTag(short e, short tag) {
        for (int s;;) {
            if ((short)(s = status) != e)
                return false;
            if (U.compareAndSwapInt(this, STATUS, s, (s & ~SMASK) | (tag & SMASK)))
                return true;
        }
    }

    static final class AdaptedRunnable<T> extends ForkJoinTask<T>
        implements RunnableFuture<T> {
        final Runnable runnable;
        T result;
        AdaptedRunnable(Runnable runnable, T result) {
            if (runnable == null) throw new NullPointerException();
            this.runnable = runnable;
            this.result = result; // OK to set this even before completion
        }
        public final T getRawResult() { return result; }
        public final void setRawResult(T v) { result = v; }
        public final boolean exec() { runnable.run(); return true; }
        public final void run() { invoke(); }
        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AdaptedRunnableAction extends ForkJoinTask<Void> implements RunnableFuture<Void> {
        final Runnable runnable;
        AdaptedRunnableAction(Runnable runnable) {
            if (runnable == null) throw new NullPointerException();
            this.runnable = runnable;
        }
        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) { }
        public final boolean exec() { runnable.run(); return true; }
        public final void run() { invoke(); }
        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class RunnableExecuteAction extends ForkJoinTask<Void> {
        final Runnable runnable;
        RunnableExecuteAction(Runnable runnable) {
            if (runnable == null) throw new NullPointerException();
            this.runnable = runnable;
        }
        public final Void getRawResult() { return null; }
        public final void setRawResult(Void v) { }
        public final boolean exec() { runnable.run(); return true; }
        void internalPropagateException(Throwable ex) {
            rethrow(ex); // rethrow outside exec() catches.
        }
        private static final long serialVersionUID = 5232453952276885070L;
    }

    static final class AdaptedCallable<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
        final Callable<? extends T> callable;
        T result;
        AdaptedCallable(Callable<? extends T> callable) {
            if (callable == null) throw new NullPointerException();
            this.callable = callable;
        }
        public final T getRawResult() { return result; }
        public final void setRawResult(T v) { result = v; }
        // 覆写核心的exec方法
        public final boolean exec() {
            try {
                result = callable.call();
                return true;
            } catch (Error err) {
                throw err;
            } catch (RuntimeException rex) {
                throw rex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        public final void run() { invoke(); }
        private static final long serialVersionUID = 2838392045355241008L;
    }
    // 用于将Runnable等接口转换成ForkJoinTask类
    public static ForkJoinTask<?> adapt(Runnable runnable) {
        return new AdaptedRunnableAction(runnable);
    }
    public static <T> ForkJoinTask<T> adapt(Runnable runnable, T result) {
        return new AdaptedRunnable<T>(runnable, result);
    }
    public static <T> ForkJoinTask<T> adapt(Callable<? extends T> callable) {
        return new AdaptedCallable<T>(callable);
    }

    // Serialization support

    private static final long serialVersionUID = -7721805057305804111L;

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        s.defaultWriteObject();
        s.writeObject(getException());
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        Object ex = s.readObject();
        if (ex != null)
            setExceptionalCompletion((Throwable)ex);
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long STATUS;

    static {
        exceptionTableLock = new ReentrantLock();
        exceptionTableRefQueue = new ReferenceQueue<Object>();
        exceptionTable = new ExceptionNode[EXCEPTION_MAP_CAPACITY];
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ForkJoinTask.class;
            STATUS = U.objectFieldOffset(k.getDeclaredField("status"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
