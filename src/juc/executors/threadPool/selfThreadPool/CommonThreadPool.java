package src.juc.executors.threadPool.selfThreadPool;

import java.util.concurrent.*;

/**
 * 普通线程池
 * @author caoyang
 */
public class CommonThreadPool extends AbstractThreadPool{
    /**
     *   单例
     */
    private volatile ThreadPoolExecutor threadPoolExecutor;

    private final int poolSize;

    private final int maxPoolSize;

    private final long keepAliveTime;

    public CommonThreadPool(int poolSize, int maxPoolSize, long keepAliveTime) {
        this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        initThreadPoolExecutor();
    }

    /**
     * DCL 双重锁检验
     */
    private void initThreadPoolExecutor(){
        if(threadPoolExecutor == null){
            synchronized (CommonThreadPool.class){
                if (threadPoolExecutor == null) {
                    BlockingQueue<Runnable> blockingQueue = new LinkedBlockingDeque<>();
                    ThreadFactory threadFactory = Executors.defaultThreadFactory();
                    RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();
                    threadPoolExecutor = new ThreadPoolExecutor(
                            // 核心线程数
                            poolSize,
                            // 最大线程数
                            maxPoolSize,
                            // 空闲时间
                            keepAliveTime,
                            // 时间单位
                            TimeUnit.MILLISECONDS,
                            // 阻塞队列
                            blockingQueue,
                            // 线程工厂
                            threadFactory,
                            // 拒绝策略
                            handler
                    );
                }
            }
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        return threadPoolExecutor.submit(task);
    }

    @Override
    public void execute(Runnable task) {
        threadPoolExecutor.execute(task);
    }

    public void remove(Runnable task){
        threadPoolExecutor.remove(task);
    }

    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }
}
