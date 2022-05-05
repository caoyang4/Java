package src.juc.executors.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
public class TestCompletableFuture2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ThreadPoolExecutor executor = TestCompletableFuture1.executor;
        CompletableFuture.runAsync(() -> {
            System.out.println("task 1 is running...");
        }, executor).whenCompleteAsync((t, u) -> {
            System.out.println("task1[whenCompleteAsync] is running...");
            System.out.println("task1[whenCompleteAsync] t:" + t);
            System.out.println("task1[whenCompleteAsync] u:" + u);
        }, executor).exceptionally((e) -> {
            System.out.println("task1[e]="+e);
            return null;
        });

        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("task 2 is running...");
            int i = 10 / 0;
            return "I'm back!";
        }, executor).whenCompleteAsync((t, u) ->{
            System.out.println("task2[whenCompleteAsync] is running...");
            System.out.println("task2[whenCompleteAsync] t:" + t);
            System.out.println("task2[whenCompleteAsync] u:" + u);
        }, executor).exceptionally((e) -> {
            System.out.println("task2[e]=" + e);
            return "I'm wrong!";
        });

        CompletableFuture<Integer> intCompletableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("task 3 is running...");
            // int i = 10 / 0;
            return 10;
        }, executor).handleAsync((t, u) -> {
            System.out.println("task3[handleAsync] t:" + t);
            System.out.println("task3[handleAsync] u:" + u);
            return 66;
        }, executor);

        System.out.println("task2[result]: "+ stringCompletableFuture.get());
        System.out.println();
        System.out.println("task3[result]: "+ intCompletableFuture.get());
        System.out.println(Thread.currentThread().getName() + " end..." );

    }
}
