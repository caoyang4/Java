package src.juc.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author caoyang
 */
public class ExecutorAndRunnable {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<String>> futureList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Future<String> future = executorService.submit(new TestCallable(i));
            futureList.add(future);
        }
        futureList.stream().forEach(
                future -> {
                    try {
                        //如果没有完成，则一直循环等待，直到Future返回完成
                        while (! future.isDone());
                        System.out.println(future.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } finally {
                        executorService.shutdown();
                    }
                }
        );
    }
}

class TestCallable implements Callable<String>{
    private int id;

    public TestCallable(int id) {
        this.id = id;
    }

    @Override
    public String call() throws Exception {
        System.out.println(Thread.currentThread().getName() + " call方法被调用");
        return Thread.currentThread().getName() + ",返回结果: "+id;
    }
}