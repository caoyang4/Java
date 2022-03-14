package src.juc.executors.threadPool;

import src.juc.executors.threadPool.monitorThread.WorkerThread;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public class TestScheduledThreadPool {
    public static void main(String[] args) throws InterruptedException {
        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2);
        System.out.println("current time: "+ new Date());
        for (int i = 1; i <= 5; i++) {
            executorService.schedule(
                    new WorkerThread("task"+i),
                    5L,
                    TimeUnit.SECONDS
                    );
        }
        Thread.sleep(5000);
        executorService.shutdown();
        while (!executorService.isTerminated()){

        }
        System.out.println("Finished all threads");
    }
}
