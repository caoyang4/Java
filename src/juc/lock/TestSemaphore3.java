package src.juc.lock;

import java.util.concurrent.Semaphore;

/**
 * @author caoyang
 */
public class TestSemaphore3 {
    public static void main(String[] args) {
        final int size = 10;
        Semaphore semaphore = new Semaphore(size);
        Thread t1 = new TestSemaphore3Thread("t1", semaphore);
        Thread t2 = new TestSemaphore3Thread("t2", semaphore);
        t1.start();
        t2.start();
        final int permits = 5;
        System.out.println(Thread.currentThread().getName() + " trying to acquire");
        try {
            semaphore.acquire(permits);
            System.out.println(Thread.currentThread().getName() + " acquire successfully");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
            System.out.println(Thread.currentThread().getName() + " release successfully");
        }
    }
}

class TestSemaphore3Thread extends Thread{
    private String name;
    private Semaphore semaphore;

    public TestSemaphore3Thread(String name, Semaphore semaphore) {
        super(name);
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        final int count = 3;
        try {
            System.out.println(Thread.currentThread().getName() + " trying to acquire");
            semaphore.acquire(count);
            System.out.println(Thread.currentThread().getName() + " acquire successfully");
            System.out.println();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaphore.release(count);
            System.out.println(Thread.currentThread().getName() + " release successfully");
        }
    }
}
