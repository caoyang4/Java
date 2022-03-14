package src.guava.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @author caoyang
 */
public class TestThreadFactoryBuilder {
    public static void main(String[] args) {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().build();
        //Common Thread Pool
        ExecutorService pool = new ThreadPoolExecutor(
                5,
                200,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
        // execute
        for (int i = 1; i <= 100; i++) {
            pool.execute(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + " do work...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        //gracefully shutdown
        pool.shutdown();
    }
}
