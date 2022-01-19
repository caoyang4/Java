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
