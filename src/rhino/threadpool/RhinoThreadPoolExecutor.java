package src.rhino.threadpool;

import src.rhino.threadpool.component.RhinoFutureTask;
import src.rhino.threadpool.component.RhinoRejectedExecutionHandler;

import java.util.concurrent.*;

/**
 * Created by zmz on 2020/11/9.
 */
public class RhinoThreadPoolExecutor extends ThreadPoolExecutor {

    private String rhinoKey;

    private boolean traceable;

    public RhinoThreadPoolExecutor(String rhinoKey, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler rejectHandler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);

        this.rhinoKey = rhinoKey;
        setRejectedExecutionHandler(rejectHandler);
    }

    public RhinoThreadPoolExecutor(String rhinoKey, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler rejectHandler, boolean traceable) {
        this(rhinoKey, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, rejectHandler);
        this.traceable = traceable;

    }

    /**
     * 重载了配置RejectedHandler的方法
     * 包装目的：拒绝请求的监控与告警逻辑
     *
     * @param handler
     */
    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler rejectHandler) {
        if (rejectHandler instanceof RhinoRejectedExecutionHandler) {
            super.setRejectedExecutionHandler(rejectHandler);
        } else {
            RhinoRejectedExecutionHandler profilingHandler = new RhinoRejectedExecutionHandler(rhinoKey, rejectHandler);
            super.setRejectedExecutionHandler(profilingHandler);
        }
    }

    /*
     包装目的：加入了MTrace透传、Cat透传、任务执行时间的监控与逻辑
     */
    @Override
    public void execute(Runnable task) {
        Runnable decorator = TaskFactory.wrap(task, rhinoKey,traceable);
        super.execute(decorator);
    }

    /*
     兼容旧版本，提交Callable时透传RhinoThreadLocal
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Callable rhinoCallable = new RhinoWrapCallable(task);
        return super.submit(rhinoCallable);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new RhinoFutureTask<T>(callable);
    }

    /**
     * 每个任务执行完成之后刷新一次线程池状态的统计数据
     *
     * @param r
     * @param t
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        int poolSize = getPoolSize();
        int activeCount = getActiveCount();
        int queueSize = getQueue().size();
        RhinoThreadPoolMetric.poolStatusLog(rhinoKey, poolSize, activeCount, queueSize);
    }
}
