package src.juc.executors.future;

import java.util.concurrent.*;

/**
 * CompletableFuture是异步执行框架
 */
public class TestCompletableFuture1 {
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5,
            10,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadPoolExecutor.AbortPolicy()
    );
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "is running...");
        }, executor);

        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "is running...");
            return "I'm back!";
        }, executor);
        System.out.println(Thread.currentThread().getName() + " end..." + stringCompletableFuture.get());
    }
}
