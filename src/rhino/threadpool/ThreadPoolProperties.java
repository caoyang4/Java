package src.rhino.threadpool;

import src.rhino.RhinoProperties;
import src.rhino.config.Configuration;
import src.rhino.threadpool.component.QueueType;
import src.rhino.threadpool.component.ResizableLinkedBlockingQueue;
import src.rhino.threadpool.component.ShutdownPolicy;

import java.util.concurrent.*;

/**
 * Created by zhanjun on 2017/4/21.
 */
public interface ThreadPoolProperties extends RhinoProperties {

    String configKeySuffix = "props";

    /**
     * default core size of thread pool
     */
    int default_coreSize = 10;

    /**
     * default max size of thread pool
     */
    int default_maxSize = 20;

    /**
     * default minutes to keep a thread alive
     */
    int default_keepAliveTimeMinutes = 1;

    /**
     * default time unit for keepAliveTime
     */
    TimeUnit default_keepAliveTimeUnit = TimeUnit.MINUTES;

    /**
     * default max size of queue, 100 , use ResizableBlockingQueue
     */
    int default_maxQueueSize = 100;

    /**
     * {@link QueueType}
     * default queue type is {@link ResizableLinkedBlockingQueue}
     */
    QueueType default_queueType = QueueType.LINKED_BLOCKING_QUEUE;

    /**
     * defualt reject handler
     */
    RejectedExecutionHandler default_RejectHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * start all core thread when thread pool is init when it is true
     */
    boolean default_preStartAllCoreThreads = false;

    /**
     * isolate the test thread pool and the normal thread pool
     */
    boolean default_testIsolate = false;

    /**
     * default shutdown policy before JVM shutdown
     */
    ShutdownPolicy default_shutdownPolicy = ShutdownPolicy.DO_NOTHING;
    /**
     * default trace
     */
    boolean default_traceable = true;

    /**
     * thread pool core size
     *
     * @return
     */
    int getCoreSize();

    /**
     * thread pool max size
     *
     * @return
     */
    int getMaxSize();

    /**
     * thread pool max queue size
     *
     * @return
     */
    int getMaxQueueSize();

    /**
     * thread pool thread alive time
     *
     * @return
     */
    int getKeepAliveTimeMinutes();

    /**
     * time unit for alive time
     *
     * @return
     */
    TimeUnit getKeepAliveTimeUnit();

    /**
     * pre start all core threads
     *
     * @return
     */
    boolean getPrestartAllCoreThreads();

    /**
     * check if isolate test and normal requests
     *
     * @return
     * @since 1.2.6.3
     */
    boolean isTestIsolate();

    /**
     * RejectedExecutionHandler
     *
     * @return
     * @since 1.2.6.4
     */
    RejectedExecutionHandler getRejectHandler();

    /**
     * shutdown policy while jvm is stopping
     *
     * @return
     */
    ShutdownPolicy getShutdownPolicy();

    /**
     * BlockingQueue
     *
     * @return
     */
    BlockingQueue<Runnable> getBlockingQueue();

    /**
     * ThreadFactory
     *
     * @return
     */
    ThreadFactory getThreadFactory();
    /**
     * is traceable
     *
     * @return
     * @since 1.3.2
     */
    boolean isTraceable();

    /**
     * update properties of the threadPool
     */
    void updateThreadPool(ThreadPool threadPool, String propStr);

    class Factory {

        /**
         * create DefaultRequestLimiterProperties with specified rhinoKey
         *
         * @param rhinoKey
         * @return
         */
        public static ThreadPoolProperties create(String rhinoKey) {
            return new DefaultThreadPoolProperties(rhinoKey);
        }

        /**
         * create DefaultRequestLimiterProperties with specified appKey, rhinoKey, config
         *
         * @param appKey
         * @param rhinoKey
         * @param config
         * @return
         */
        public static ThreadPoolProperties create(String appKey, String rhinoKey, Configuration config) {
            return new DefaultThreadPoolProperties(appKey, rhinoKey, null, config);
        }
    }
}
