package src.juc.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 调用Condition的await()和signal()方法，都必须在lock保护之内，
 * 就是说必须在lock.lock()和lock.unlock之间才可以使用
 * @author caoyang
 */
public class TestCondition {
    private Lock lock = new ReentrantLock(false);
    private Condition condition = lock.newCondition();

    public void doSendSingal(){
        lock.lock();
        try {
            System.out.println("begin to send signal");
            // 仅唤醒线程，并未释放锁
            condition.signalAll();
            System.out.println("end to send signal");
        } finally {
            // 此处释放锁
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
