package src.juc.executors.future;

import java.util.concurrent.*;

/**
 * @author caoyang
 */
public class TestFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int x = 0;
                for (int i = 0; i < 10; i++) {
                    x++;
                    System.out.println(x);
                    Thread.sleep(1000);
                }
                return x;
            }
        });
        Integer res = future.get();
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println("final: " + res);

    }
}
