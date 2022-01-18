package src.juc.thread;

import java.util.concurrent.*;

/**
 * @author caoyang
 */
public class TestThread {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("MainThread 启动...");

        // [1]继承Thread创建线程
        Thead1 thead1 = new Thead1();
        thead1.setName("Thread1");
        thead1.start();

        // [2]实现Runnable接口创建线程
        Thread thead2 = new Thread(
                new Thread2()
        );
        thead2.setName("Thread2");
        thead2.start();

        new Thread(() ->{
            System.out.println(Thread.currentThread().getName() + " 启动...");
        }, "Thread3").start();

        // [3]实现callable接口实现线程
        Callable<String> call = () -> {
            Thread.sleep(1000);
            return "Hi, Callable";
        };
        // lambda 表达式
        /*Callable<String> call = () -> "Callable 启动线程...";*/
        FutureTask<String> stringFutureTask = new FutureTask<>(call);
        Thread thread4 = new Thread(stringFutureTask, "实现Callable接口创建");
        thread4.start();
        System.out.println(thread4.getName() + ", Callable线程返回值：" + stringFutureTask.get());

        // [4]线程池方式创建线程
        /**
         * execute会首先在线程池中选择一个空闲线程来执行任务，
         * 如果线程池中没有空闲线程，它便会创建一个新的线程来执行任务。
         */
        ExecutorService executorService = Executors.newCachedThreadPool();
        /*
        ExecutorService executorService = Executors.newFixedThreadPool();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        */
        for (int i = 0; i < 100; i++) {
            executorService.execute(new TestRunnable());
            System.out.println("************* task" + i + " *************");
        }
        executorService.shutdown();
    }
}

class Thead1 extends Thread{
    @Override
    public void run() {
        System.out.println(currentThread().getName() + " 以继承Thread方式创建...");
    }
}

class Thread2 implements Runnable{
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " 以实现Runnable接口创建...");
    }
}

class TestRunnable implements Runnable{
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "线程启用");
    }
}
