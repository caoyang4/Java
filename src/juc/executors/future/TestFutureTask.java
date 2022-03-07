package src.juc.executors.future;

import java.util.concurrent.*;

/**
 * @author caoyang
 */
public class TestFutureTask {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int x = 0;
                while (++x != 10){
                    Thread.sleep(1000);
                    System.out.println(x);
                }
                return x;
            }
        });

        executorService.submit(futureTask);
        int res = futureTask.get();
        System.out.println("final: " + res);
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}
