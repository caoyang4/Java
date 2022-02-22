package src.juc.threadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 单线程池
 * @author caoyang
 */
public class SingleThreadPool extends AbstractThreadPool{
    private final ExecutorService executorService;

    /**
     * 单线程池通过 ThreadPoolExecutor 创建
     * new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())
     * 核心线程数和最大线程数都为 1
     * 默认线程工厂defaultThreadFactory
     * 默认AbortPolicy拒绝策略
     */
    public SingleThreadPool() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @Override
    public void execute(Runnable task) {
        executorService.execute(task);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}
