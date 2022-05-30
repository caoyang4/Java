package src.rhino.threadpool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;
import src.rhino.threadpool.component.ResizableBlockingQueue;
import src.rhino.threadpool.component.ShutdownPolicy;
import src.rhino.util.CommonUtils;
import src.rhino.util.PlatformSpecific;

/**
 * 线程池装饰类，封装了动态配置的逻辑
 *
 * @author zhanjun on 2017/4/21.
 */
public class DefaultThreadPool implements ThreadPool {

    private static Logger logger = LoggerFactory.getLogger(DefaultThreadPool.class);

    private String rhinoKey;
    private ThreadPoolProperties poolProperties;
    private ThreadPoolExecutor executor;

    public DefaultThreadPool(String rhinoKey, ThreadPoolProperties poolProperties) {
        this.rhinoKey = rhinoKey;
        this.poolProperties = poolProperties;
        this.executor = newExecutor();

        if (poolProperties.getPrestartAllCoreThreads()) {
            executor.prestartAllCoreThreads();
        }
    }

    /**
     * 初始化线程池对象
     *
     * @return
     */
    private ThreadPoolExecutor newExecutor() {
        int coreSize = poolProperties.getCoreSize();
        int maximumSize = poolProperties.getMaxSize();
        int keepAliveTime = poolProperties.getKeepAliveTimeMinutes();
        TimeUnit unitForAliveTime = poolProperties.getKeepAliveTimeUnit();
        BlockingQueue<Runnable> blockingQueue = getThreadPoolProperties().getBlockingQueue();
        ThreadFactory threadFactory = poolProperties.getThreadFactory() == null ? defaultThreadFactory() : poolProperties.getThreadFactory();
        RejectedExecutionHandler rejectHandler = poolProperties.getRejectHandler();

        CommonUtils.assertTrue(coreSize > 0, "coreSize must be greater than 0: " + rhinoKey);
        CommonUtils.assertTrue(maximumSize > 0, "maximumSize must be greater than 0: " + rhinoKey);
        CommonUtils.assertTrue(maximumSize >= coreSize, "maximumSize must be greater than coreSize: " + rhinoKey);
        CommonUtils.assertTrue(keepAliveTime > 0, "keepAliveTime must be greater than 0: " + rhinoKey);
        CommonUtils.assertTrue(rejectHandler != null, "reject hanlder can not be null: " + rhinoKey);

        return new RhinoThreadPoolExecutor(rhinoKey, coreSize, maximumSize, keepAliveTime, unitForAliveTime, blockingQueue, threadFactory, rejectHandler, poolProperties.isTraceable());
    }

    /**
     * 默认使用rhinokey作为线程名称，用户可通过executor对象自定义替换
     *
     * @return
     */
    private ThreadFactory defaultThreadFactory() {
        if (!PlatformSpecific.isAppEngineStandardEnvironment()) {
            return new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "Rhino-" + rhinoKey + "-" + threadNumber.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                }
            };
        } else {
            return PlatformSpecific.getAppEngineThreadFactory();
        }
    }

    @Override
    public Future submit(Callable task) {
        return executor.submit(task);
    }

    @Override
    public Future submit(Runnable task) {
        return executor.submit(task);
    }

    @Override
    public void execute(Runnable task) {
        executor.execute(task);
    }

    @Override
    public <T> Future submit(Runnable task, T result) {
        return executor.submit(task, result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executor.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executor.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public String getRhinoKey() {
        return rhinoKey;
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    @Override
    public ThreadPoolProperties getThreadPoolProperties() {
        return poolProperties;
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        this.executor.setCorePoolSize(corePoolSize);
    }

    @Override
    public void setMaxPoolSize(int maxPoolSize) {
        this.executor.setMaximumPoolSize(maxPoolSize);
    }

    @Override
    public BlockingQueue<Runnable> getWorkQueue() {
        return executor.getQueue();
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        executor.allowCoreThreadTimeOut(value);
    }

    @Override
    public int prestartAllCoreThreads() {
        return executor.prestartAllCoreThreads();
    }

    @Override
    public void setWorkQueueCapacity(int queueCapacity) {
        BlockingQueue<Runnable> currentWorkQueue = getWorkQueue();
        if (currentWorkQueue instanceof ResizableBlockingQueue) {
            ((ResizableBlockingQueue) currentWorkQueue).setCapacity(queueCapacity);
        }
    }

    @Override
    public void setRejectHandler(RejectedExecutionHandler rejectHandler) {
        this.executor.setRejectedExecutionHandler(rejectHandler);
    }

    @Override
    public void shutdown() {
        this.shutdown(false);

    }

    @Override
    public void shutdown(boolean remove) {
        try {
            ShutdownPolicy shutdownPolicy = poolProperties.getShutdownPolicy();
            shutdownPolicy.shutdown(this.executor);
            if (remove) {
                ThreadPool.Factory.remove(this.rhinoKey);
            }
            logger.info(rhinoKey + " theadpool shutdown success");
        } catch (Exception e) {
            logger.info(rhinoKey + " theadpool shutdown failed: " + e.getMessage());
        }

    }

    /**
     * 手动关闭线程池
     *
     * @param policy 关闭策略
     * @throws InterruptedException
     */
    @Override
    public void shutdown(ShutdownPolicy policy) throws InterruptedException {
        this.shutdown(policy, false);
    }

    @Override
    public void shutdown(ShutdownPolicy policy, boolean remove) throws InterruptedException {
        CommonUtils.assertTrue(policy != null, rhinoKey + " threadpool shutdown failed: shutdown policy can't be null!");
        policy.shutdown(this.executor);
        if (remove) {
            ThreadPool.Factory.remove(this.rhinoKey);
        }
    }
}
