package java.util.concurrent;

/**
 * CountedCompleter 继承了 ForkJoinTask，即为一个异步任务
 */
public abstract class CountedCompleter<T> extends ForkJoinTask<T> {
    private static final long serialVersionUID = 5232453752276485070L;
    // 用于指向分解该任务的任务，即父任务
    final CountedCompleter<?> completer;
    // 未完成的任务个数
    volatile int pending;

    /**
     * @param completer 父任务
     * @param initialPendingCount 初始的未完成任务个数
     */
    protected CountedCompleter(CountedCompleter<?> completer, int initialPendingCount) {
        this.completer = completer;
        this.pending = initialPendingCount;
    }

    protected CountedCompleter(CountedCompleter<?> completer) {
        this.completer = completer;
    }

    protected CountedCompleter() {
        this.completer = null;
    }
    // 任务执行的方法，抽象方法交由子类实现
    public abstract void compute();
    // 完成当前任务时执行的钩子方法，类似于后置函数，默认空实现，交给子类自己安排
    public void onCompletion(CountedCompleter<?> caller) {
    }

    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        return true;
    }
    // 获取当前任务的父任务
    public final CountedCompleter<?> getCompleter() {
        return completer;
    }

    public final int getPendingCount() {
        return pending;
    }

    public final void setPendingCount(int count) {
        pending = count;
    }
    // 添加未完成的任务个数
    public final void addToPendingCount(int delta) {
        U.getAndAddInt(this, PENDING, delta);
    }

    public final boolean compareAndSetPendingCount(int expected, int count) {
        return U.compareAndSwapInt(this, PENDING, expected, count);
    }
    // CAS 设置 未完成的任务个数 -1，为 0 则无需操作
    public final int decrementPendingCountUnlessZero() {
        int c;
        do {} while ((c = pending) != 0 &&
                     !U.compareAndSwapInt(this, PENDING, c, c - 1));
        return c;
    }
    // 获取最顶点的任务，即未进行分解的任务
    public final CountedCompleter<?> getRoot() {
        CountedCompleter<?> a = this, p;
        while ((p = a.completer) != null)
            a = p;
        return a;
    }
    // 尝试设置完成当前任务
    public final void tryComplete() {
        CountedCompleter<?> a = this, s = a;
        for (int c;;) {
            // pending为 0 代表当前没有未完成的任务
            if ((c = a.pending) == 0) {
                a.onCompletion(s);
                // 递归父任务，找到顶任务
                if ((a = (s = a).completer) == null) {
                    s.quietlyComplete();
                    return;
                }
            }
            // CAS 设置 pending - 1
            else if (U.compareAndSwapInt(a, PENDING, c, c - 1))
                return;
        }
    }
    // 传播完成
    // 循环设置 pending--，直至为 0 时设置顶任务，执行s.quietlyComplete()
    public final void propagateCompletion() {
        CountedCompleter<?> a = this, s = a;
        for (int c;;) {
            if ((c = a.pending) == 0) {
                if ((a = (s = a).completer) == null) {
                    s.quietlyComplete();
                    return;
                }
            }
            else if (U.compareAndSwapInt(a, PENDING, c, c - 1))
                return;
        }
    }

    public void complete(T rawResult) {
        CountedCompleter<?> p;
        setRawResult(rawResult);
        onCompletion(this);
        // 设置 status 为 NORMAL
        quietlyComplete();
        if ((p = completer) != null)
            p.tryComplete();
    }
    //  第一个完成任务的 Task
    public final CountedCompleter<?> firstComplete() {
        for (int c;;) {
            if ((c = pending) == 0)
                return this;
            else if (U.compareAndSwapInt(this, PENDING, c, c - 1))
                return null;
        }
    }
    // 下一个完成任务的Task
    // 所谓分而治之，下一个完成的task就是它的父任务，因为完成之后是合并，顶任务为 null
    public final CountedCompleter<?> nextComplete() {
        CountedCompleter<?> p;
        if ((p = completer) != null)
            return p.firstComplete();
        else {
            quietlyComplete();
            return null;
        }
    }
    // 循环找到顶任务，设置其状态为 NORMAL
    public final void quietlyCompleteRoot() {
        for (CountedCompleter<?> a = this, p;;) {
            if ((p = a.completer) == null) {
                a.quietlyComplete();
                return;
            }
            a = p;
        }
    }
    // 帮助完成任
    public final void helpComplete(int maxTasks) {
        Thread t; ForkJoinWorkerThread wt;
        if (maxTasks > 0 && status >= 0) {
            if ((t = Thread.currentThread()) instanceof ForkJoinWorkerThread)
                (wt = (ForkJoinWorkerThread)t).pool.
                    helpComplete(wt.workQueue, this, maxTasks);
            else
                // 默认 common 线程池
                ForkJoinPool.common.externalHelpComplete(this, maxTasks);
        }
    }

    void internalPropagateException(Throwable ex) {
        CountedCompleter<?> a = this, s = a;
        while (a.onExceptionalCompletion(ex, s) &&
               (a = (s = a).completer) != null && a.status >= 0 &&
               a.recordExceptionalCompletion(ex) == EXCEPTIONAL)
            ;
    }

    protected final boolean exec() {
        compute();
        return false;
    }

    public T getRawResult() { return null; }

    protected void setRawResult(T t) { }

    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long PENDING;
    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            PENDING = U.objectFieldOffset(CountedCompleter.class.getDeclaredField("pending"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
