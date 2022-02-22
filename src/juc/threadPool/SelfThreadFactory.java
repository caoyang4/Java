package src.juc.threadPool;

/**
 * 自定义线程工厂
 * @author caoyang
 */
public class SelfThreadFactory {

    private SelfThreadFactory(){}

    private static volatile ThreadPoolInterface selfPool;


    public enum ThreadFactoryEnum {
        /**
         * 单例
         */
        INSTANCE;

        /**
         * 单线程池
         */
        private final ThreadPoolInterface singlePool;

        /**
         * 默认线程池
         */
        private final ThreadPoolInterface defaultNormalPool;

        /**
         * 周期任务线程池
         */
        private final ScheduledThreadPool scheduledThreadPool;

        ThreadFactoryEnum() {
            singlePool = new SingleThreadPool();
            defaultNormalPool = new CommonThreadPool(5, 10, 3000);
            scheduledThreadPool = new ScheduledThreadPool(5);
        }

        private ThreadPoolInterface getSinglePool(){
            return singlePool;
        }

        private ThreadPoolInterface getDefaultNormalPool(){
            return defaultNormalPool;
        }
        private ScheduledThreadPool getScheduledThreadPool(){
            return scheduledThreadPool;
        }
    }

    /**
     * 获取单线程池
     * @return
     */
    public static ThreadPoolInterface getSinglePool(){
        return ThreadFactoryEnum.INSTANCE.getSinglePool();
    }

    /**
     * 获取默认配置线程池
     * @return
     */
    public static ThreadPoolInterface getDefaultNormalPool(){
        return ThreadFactoryEnum.INSTANCE.getDefaultNormalPool();
    }

    /**
     * 获取周期任务线程池
     * @return
     */
    public static ScheduledThreadPool getScheduledThreadPool(){
        return ThreadFactoryEnum.INSTANCE.getScheduledThreadPool();
    }

    /**
     * 获取自定义线程池，通过 ThreadPoolExecutor 全参构造函数配置
     * ThreadPoolExecutor(int corePoolSize,
     *                    int maximumPoolSize,
     *                    long keepAliveTime,
     *                    TimeUnit unit,
     *                    BlockingQueue<Runnable> workQueue,
     *                    ThreadFactory threadFactory,
     *                    RejectedExecutionHandler handler)
     * @param poolSize
     * @param maxPoolSize
     * @param keepAliveTime
     * @return
     */
    public static ThreadPoolInterface getSelfPool(int poolSize, int maxPoolSize, long keepAliveTime){
        if (selfPool == null) {
            synchronized (SelfThreadFactory.class){
                if (selfPool == null){
                   selfPool = new CommonThreadPool(poolSize, maxPoolSize, keepAliveTime);
                }
            }
        }
        return selfPool;
    }


}
