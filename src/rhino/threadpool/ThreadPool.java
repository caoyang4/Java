package src.rhino.threadpool;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import src.rhino.threadpool.component.ShutdownPolicy;
import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoType;
import src.rhino.RhinoUseMode;
import src.rhino.config.ConfigChangedListener;
import src.rhino.service.RhinoEntity;
import src.rhino.service.RhinoManager;
import src.rhino.threadpool.alarm.ThreadPoolAlarmManager;
import src.rhino.threadpool.job.RhinoThreadPoolJob;
import src.rhino.util.AppUtils;
import src.rhino.util.CommonUtils;

/**
 * @author zhanjun on 2017/4/21.
 */
public interface ThreadPool {

    /**
     * rhino key
     *
     * @return
     */
    String getRhinoKey();

    /**
     * submit a task to thread pool and run
     *
     * @param task
     * @return
     */
    Future submit(Callable task);


    /**
     * submit a task in thread pool
     *
     * @param task
     */
    Future submit(Runnable task);

    /**
     * submit a task in thread poll
     *
     * @param task
     * @param result
     * @param <T>
     * @return
     */
    <T> Future submit(Runnable task, T result);

    /**
     * execute a task in thread pool
     *
     * @param task
     */
    void execute(Runnable task);

    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;

    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException;

    <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;

    <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * get thread pool executor
     *
     * @return
     */
    ThreadPoolExecutor getExecutor();

    /**
     * get resizeable blocking queue
     *
     * @return
     */
    BlockingQueue<Runnable> getWorkQueue();

    /**
     * get properties for thread pool
     *
     * @return
     */
    ThreadPoolProperties getThreadPoolProperties();

    /**
     * set core size for thread pool when corSize is changed
     *
     * @param corePoolSize
     */
    void setCorePoolSize(int corePoolSize);

    /**
     * set max size for thread pool when maxSize is changed
     *
     * @param maxPoolSize
     */
    void setMaxPoolSize(int maxPoolSize);

    /**
     * set woke queue capacity for thread pool when maxQueueSize is changed
     *
     * @param queueCapacity
     */
    void setWorkQueueCapacity(int queueCapacity);


    void setRejectHandler(RejectedExecutionHandler rejectHandler);


    void shutdown();

    void shutdown(boolean remove);

    void shutdown(ShutdownPolicy policy) throws InterruptedException;

    void shutdown(ShutdownPolicy policy,boolean remove) throws InterruptedException;

    void allowCoreThreadTimeOut(boolean value);

    int prestartAllCoreThreads();

    class Factory {

        private static ThreadPool EMPTY = null;

        private static final Map<String, ThreadPool> threadPools = new ConcurrentHashMap<>();

        static {
            ThreadPoolAlarmManager.getInstance();
            RhinoThreadPoolJob.init();
        }

        /**
         * @param rhinoKey
         * @return
         */
        public static ThreadPool getInstance(String rhinoKey) {
            return getInstance(rhinoKey, null);
        }

        public static ThreadPool getInstance(String rhinoKey, ThreadPoolProperties threadPoolProperties) {
            return getInstance(rhinoKey, threadPoolProperties, RhinoUseMode.API);
        }

        /**
         * return or create specified thread pool key and properties for thread pool
         *
         * @param rhinoKey
         * @param threadPoolProperties
         * @return
         */
        public static ThreadPool getInstance(String rhinoKey, ThreadPoolProperties threadPoolProperties, RhinoUseMode useMode) {
            if (StringUtils.isNullOrEmpty(rhinoKey)) {
                return EMPTY;
            }

            ThreadPool threadPool = threadPools.get(rhinoKey);
            if (threadPool == null) {
                synchronized (DefaultThreadPool.class) {
                    if (!threadPools.containsKey(rhinoKey)) {
                        if (threadPoolProperties == null) {
                            threadPoolProperties = new DefaultThreadPoolProperties(rhinoKey);
                        }
                        threadPool = new ThreadPoolProxy(rhinoKey, threadPoolProperties);
                        addPropertiesChangedListener(threadPoolProperties, threadPool);
                        RhinoManager.report(new RhinoEntity(rhinoKey, RhinoType.ThreadPool, useMode.getValue(), AppUtils.getSet(), CommonUtils.parseProperties((DefaultThreadPoolProperties) threadPoolProperties)));
                        threadPools.put(rhinoKey, threadPool);
                    }
                    threadPool = threadPools.get(rhinoKey);
                }
            }
            return threadPool;
        }

        /**
         * @param threadPoolProperties
         * @param threadPool
         */
        private static void addPropertiesChangedListener(final ThreadPoolProperties threadPoolProperties, final ThreadPool threadPool) {
            threadPoolProperties.addConfigChangedListener(new ConfigChangedListener() {
                private final Object lock = new Object();

                @Override
                public void invoke(String key, String oldValue, String newValue) {
                    synchronized (lock) {
                        if (newValue == null || newValue.equals(oldValue)) {
                            return;
                        }
                        threadPoolProperties.updateThreadPool(threadPool, newValue);
                    }
                }
            });
        }

        /**
         * empty one
         *
         * @return
         */
        public static ThreadPool getEmpty() {
            return EMPTY;
        }

        /**
         * 关闭当前所有线程池
         */
        public static void shutDownThreadPools() {
            for (ThreadPool pool : threadPools.values()) {
                pool.shutdown();
            }
        }

        /**
         * get from cache
         *
         * @param rhinoKey
         * @return
         */
        public static ThreadPool get(String rhinoKey) {
            return threadPools.get(rhinoKey);
        }

        /**
         * remove from cache
         *
         * @param rhinoKey
         * @return
         */
        public static ThreadPool remove(String rhinoKey) {
            return threadPools.remove(rhinoKey);
        }
    }
}