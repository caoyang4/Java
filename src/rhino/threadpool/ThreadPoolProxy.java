package src.rhino.threadpool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import src.rhino.threadpool.component.ShutdownPolicy;
import src.rhino.util.MtraceUtils;

/**
 * Created by zmz on 2020/4/8.
 */
public class ThreadPoolProxy implements ThreadPool {

    private String rhinoKey;
    private ThreadPoolProperties poolProperties;
    private ThreadPool normalThreadPool;
    private volatile ThreadPool testThreadPool;

    public ThreadPoolProxy(String rhinokey, ThreadPoolProperties poolProperties) {
        this.rhinoKey = rhinokey;
        this.poolProperties = poolProperties;
        this.normalThreadPool = new DefaultThreadPool(rhinoKey, poolProperties);
    }

    private ThreadPool getThreadPool() {
        if (this.poolProperties.isTestIsolate()) {
            return MtraceUtils.isTest() ? getTestThreadPool() : getNormalThreadPool();
        } else {
            return getNormalThreadPool();
        }
    }

    //压测流量线程池
    private ThreadPool getTestThreadPool() {
        if (testThreadPool == null) {
            synchronized (this) {
                if (testThreadPool == null) {
                    testThreadPool = new DefaultThreadPool(rhinoKey + MtraceUtils.TEST_FLAG, poolProperties);
                    //为了避免空闲压测线程长期浪费资源，允许压测线程池的core线程被自动回收
                    testThreadPool.allowCoreThreadTimeOut(true);
                }
            }
        }
        return testThreadPool;
    }

    //正常流量线程池
    private ThreadPool getNormalThreadPool() {
        return normalThreadPool;
    }

    @Override
    public String getRhinoKey() {
        return getThreadPool().getRhinoKey();
    }

    @Override
    public Future submit(Callable task) {
        return getThreadPool().submit(task);
    }

    @Override
    public void execute(Runnable task) {
        getThreadPool().execute(task);
    }

    @Override
    public Future submit(Runnable task) {
        return getThreadPool().submit(task);
    }

    @Override
    public <T> Future submit(Runnable task, T result) {
        return getThreadPool().submit(task, result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return getThreadPool().invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return getThreadPool().invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return getThreadPool().invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getThreadPool().invokeAny(tasks, timeout, unit);
    }

    @Override
    public void allowCoreThreadTimeOut(boolean value) {
        getThreadPool().allowCoreThreadTimeOut(value);
    }

    @Override
    public int prestartAllCoreThreads() {
        return getThreadPool().prestartAllCoreThreads();
    }

    @Override
    public ThreadPoolExecutor getExecutor() {
        return getThreadPool().getExecutor();
    }

    @Override
    public BlockingQueue<Runnable> getWorkQueue() {
        return getThreadPool().getWorkQueue();
    }

    @Override
    public ThreadPoolProperties getThreadPoolProperties() {
        return getThreadPool().getThreadPoolProperties();
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        normalThreadPool.setCorePoolSize(corePoolSize);
        if (testThreadPool != null) {
            testThreadPool.setCorePoolSize(corePoolSize);
        }
    }

    @Override
    public void setMaxPoolSize(int maxPoolSize) {
        normalThreadPool.setMaxPoolSize(maxPoolSize);
        if (testThreadPool != null) {
            testThreadPool.setMaxPoolSize(maxPoolSize);
        }
    }

    @Override
    public void setWorkQueueCapacity(int queueCapacity) {
        normalThreadPool.setWorkQueueCapacity(queueCapacity);
        if (testThreadPool != null) {
            testThreadPool.setWorkQueueCapacity(queueCapacity);
        }
    }

    @Override
    public void setRejectHandler(RejectedExecutionHandler rejectHandler) {
        normalThreadPool.setRejectHandler(rejectHandler);
        if (testThreadPool != null) {
            testThreadPool.setRejectHandler(rejectHandler);
        }
    }

    @Override
    public void shutdown() {
        this.shutdown(false);
    }

    @Override
    public void shutdown(boolean remove) {
        normalThreadPool.shutdown(remove);
        if (testThreadPool != null) {
            testThreadPool.shutdown(remove);
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
        normalThreadPool.shutdown(policy, remove);
        if (testThreadPool != null) {
            testThreadPool.shutdown(policy, remove);
        }
    }
}
