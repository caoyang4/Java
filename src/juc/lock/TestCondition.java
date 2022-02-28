package src.juc.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestCondition {
    private Lock lock = new ReentrantLock(false);
    private Condition condition = lock.newCondition();

    public void doSendSingal(){
        lock.lock();
        try {
            System.out.println("begin to send signal");
            condition.signalAll();
            System.out.println("end to send signal");
        } finally {
            lock.unlock();
        }
    }

    public void doAwait(){
        lock.lock();
        try {
            System.out.println("begin to await");
            condition.await();
            System.out.println("end to await");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        TestCondition test = new TestCondition();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(() -> test.doAwait());
        executorService.execute(() -> test.doSendSingal());
    }
}
