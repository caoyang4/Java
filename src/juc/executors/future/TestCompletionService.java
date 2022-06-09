package src.juc.executors.future;

import java.util.concurrent.*;

/**
 * @author caoyang
 * @create 2022-06-09 11:52
 */
public class TestCompletionService {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        ExecutorCompletionService<Result> completionService = new ExecutorCompletionService<>(executorService);
        for (int i = 0; i < 10; i++) {
            Task task = new Task("task" + i);
            System.out.println("submit: "+task);
            completionService.submit(task);
        }
        System.out.println();
        for (int i = 0; i < 10; i++) {
            System.out.println("get: "+completionService.take().get());
        }
    }
    static class Result{
        String name;
        public Result(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }
    static class Task implements Callable<Result>{
        private String taskName;
        public Task(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public Result call() throws Exception {
            return new Result(Thread.currentThread().getName()+ "-" + taskName);
        }

        @Override
        public String toString() {
            return "Task{" +
                    "taskName='" + taskName + '\'' +
                    '}';
        }
    }
}
