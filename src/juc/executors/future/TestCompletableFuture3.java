package src.juc.executors.future;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * tCompletableFuture任务编排
 */
@Slf4j(topic = "TestCompletableFuture3")
public class TestCompletableFuture3 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final ThreadPoolExecutor executor = TestCompletableFuture1.executor;
        CompletableFuture.runAsync(() -> {
            log.info("task 1 is running...");
            JucUtils.sleepSeconds(1);
        }, executor).thenRunAsync(() -> {
            log.info("task 2 is running...");
        }, executor);
        JucUtils.sleepSeconds(3);

        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            log.info("future1 start...");
            JucUtils.sleepSeconds(3);
            log.info("future1 end...");
            return 666;
        }, executor);

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            log.info("future2 start...");
            JucUtils.sleepSeconds(1);
            log.info("future2 end...");
            return "I'm back!";
        }, executor);
        // 两个任务任一执行完成
        future1.runAfterEitherAsync(future2,()->{
            log.info("runAfterEitherAsync");
        },executor);

        // 两个任务都完成
        future1.thenAcceptBothAsync(future2, (f1, f2) -> {
            System.out.println(f2 + " " + f1);
        });
        future1.runAfterBothAsync(future2, () -> {
            log.info("future3 ...");
        });
        CompletableFuture<String> future3 = future1.thenCombineAsync(future2, (f1, f2) -> f2 + " naive " + f1, executor);
        log.info("thenCombineAsync :{}", future3.get());
        log.info("main end...");
    }
}
