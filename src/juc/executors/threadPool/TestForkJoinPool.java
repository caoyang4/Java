package src.juc.executors.threadPool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * @author caoyang
 */
public class TestForkJoinPool  {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool pool1 = new ForkJoinPool();
        ForkJoinTask<Integer> taskSum = new SumTask(1, 10000);
        pool1.submit(taskSum);
        int result1 = taskSum.get();
        System.out.println("Sum(1,10000): "+result1);
        System.out.println();
        ForkJoinPool pool2 = new ForkJoinPool(4);
        ForkJoinTask<Integer> taskFib = new Fibonacci(20);
        pool2.submit(taskFib);
        int result2 = taskFib.get();
        System.out.println("Fibonacci(20): " + result2);
    }
    static class SumTask extends RecursiveTask<Integer>{
        private static final long serialVersionUID = 1L;
        private int start;
        private int end;

        public SumTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start < 1000){
                int sum = 0;
                System.out.println(Thread.currentThread().getName() + " 开始执行: " + start + "-" + end);
                for (int i = start; i <= end; i++) {
                    sum += i;
                }
                return sum;
            }
            int middle = start + ((end - start) >> 1);
            SumTask task1 = new SumTask(start, middle);
            SumTask task2 = new SumTask(middle+1, end);
            task1.fork();
            task2.fork();
            return task1.join() + task2.join();
        }
    }

    static class Fibonacci extends RecursiveTask<Integer>{
        private static final long serialVersionUID = 2L;
        private int n;

        public Fibonacci(int n) {
            this.n = n;
        }

        @Override
        protected Integer compute() {
            if (n <= 2){
                return 1;
            }
            Fibonacci f1 = new Fibonacci(n - 1);
            f1.fork();
            Fibonacci f2 = new Fibonacci(n - 2);
            return f2.compute() + f1.join();
        }
    }
}
