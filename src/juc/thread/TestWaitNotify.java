package src.juc.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestWaitNotify {
    public synchronized void doNotify(){
        System.out.println("begin to notify");
        notifyAll();
        System.out.println("end to notify");
    }
    public synchronized void doWait(){
        try {
            System.out.println("begin to wait");
            wait();
            System.out.println("end to wait");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        TestWaitNotify test = new TestWaitNotify();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> test.doWait());
        executorService.execute(() -> test.doNotify());
        executorService.shutdown();
    }
}
