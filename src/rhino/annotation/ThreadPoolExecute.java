package src.rhino.annotation;

import src.rhino.threadpool.ThreadPoolProperties;
import src.rhino.threadpool.component.QueueType;
import src.rhino.threadpool.component.ResizableBlockingQueue;
import src.rhino.threadpool.component.ShutdownPolicy;

import java.lang.annotation.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhanjun on 2017/4/21.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ThreadPoolExecute {

    /**
     * thread pool key
     *
     * @return
     */
    String rhinoKey();

    /**
     * core thread size of pool, default is 10
     *
     * @return
     */
    int coreSize() default ThreadPoolProperties.default_coreSize;

    /**
     * max thread size of pool, default is 10
     *
     * @return
     */
    int maxSize() default ThreadPoolProperties.default_maxSize;

    /**
     * minutes to keep a thread alive, default is 1 minutes
     *
     * @return
     */
    int keepAliveTimeMinutes() default ThreadPoolProperties.default_keepAliveTimeMinutes;

    /**
     * time unit for keepAliveTime
     *
     * @return
     */
    TimeUnit keepAliveTimeUnit() default TimeUnit.MINUTES;

    /**
     * max queue size
     *
     * @return
     */
    int maxQueueSize() default ThreadPoolProperties.default_maxQueueSize;

    /**
     * blocking queue，default will be null
     *
     * @return
     */
    Class<? extends BlockingQueue> blockingQueue() default ResizableBlockingQueue.class;

    /**
     * reject handler
     *
     * @return
     */
    Class<? extends RejectedExecutionHandler> rejectHandler() default ThreadPoolExecutor.AbortPolicy.class;

    /**
     * prestart all core threads
     *
     * @return
     */
    boolean prestartAllCoreThreads() default ThreadPoolProperties.default_preStartAllCoreThreads;

    /**
     * check if isolate normal requests and test requests
     *
     * @return
     */
    boolean testIsolate() default ThreadPoolProperties.default_testIsolate;

    /**
     * shutdown policy
     *
     * @return
     */
    ShutdownPolicy shutdownPolicy() default ShutdownPolicy.DO_NOTHING;

    /**
     * is trace
     */
    boolean traceable() default ThreadPoolProperties.default_traceable;

    /**
     * 队列类型，默认是 LINKED_BLOCKING_QUEUE
     *
     * @return
     */
    QueueType queueType() default QueueType.LINKED_BLOCKING_QUEUE;
}
