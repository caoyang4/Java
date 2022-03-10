package src.juc.executors.threadPool.monitorThread;

import java.util.concurrent.*;

/**
 * @author caoyang
 */
public class TestMonitorThread {
    public static void main(String[] args) throws InterruptedException {
        final int workerSize = 100;
        RejectedExecutionHandlerImpl rejectedExecutionHandler = new RejectedExecutionHandlerImpl();
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,
                4,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(4),
                threadFactory,
                rejectedExecutionHandler
        );
        MonitorThread monitorThread = new MonitorThread(executor, 1);
        new Thread(monitorThread).start();

        for (int i = 1; i <= workerSize; i++) {
            executor.execute(new WorkerThread("task"+i));
        }
        Thread.sleep(3000);
        monitorThread.shutdown();
        Thread.sleep(5000);
        executor.shutdown();
    }
}
