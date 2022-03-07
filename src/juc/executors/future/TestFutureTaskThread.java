package src.juc.executors.future;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author caoyang
 */
public class TestFutureTaskThread {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        FutureTask<Integer> futureTask = new FutureTask<>(new MyTask());
        Thread thread = new Thread(futureTask, "AddTask");
        thread.start();
        Thread.sleep(1000);
        while (!futureTask.isDone()){
            System.out.println("Task is not done");
            Thread.sleep(1000);
        }
        System.out.println("Task is done!");
        int res = futureTask.get();
        System.out.println("final: " + res);
    }

    static class MyTask implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            System.out.println("Thread [" + Thread.currentThread().getName() + "] is running");
            int result = 0;

            for(int i = 0; i < 100;++i) {
                result += i;
            }

            Thread.sleep(3000);
            return result;
        }
    }
}
