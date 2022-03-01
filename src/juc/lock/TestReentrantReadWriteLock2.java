package src.juc.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author caoyang
 */
public class TestReentrantReadWriteLock2 {
    public static void main(String[] args) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        List<TestReentrantReadWriteLock2Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestReentrantReadWriteLock2Thread thread = null;
            if( i < 2) {
                thread = new TestReentrantReadWriteLock2Thread("ThreadWrite" + (i + 1), false, lock);
            } else if( i < 8){
                thread = new TestReentrantReadWriteLock2Thread("ThreadRead"+(i-1), true, lock);
            } else {
                thread = new TestReentrantReadWriteLock2Thread("ThreadWrite"+(i-5), false, lock);
            }
            threadList.add(thread);
        }
        for (TestReentrantReadWriteLock2Thread thread : threadList) {
            thread.start();
        }
    }
}

class TestReentrantReadWriteLock2Thread extends Thread{
    Lock readLock;
    Lock writeLock;
    private boolean read;

    public TestReentrantReadWriteLock2Thread(String name, boolean read, ReentrantReadWriteLock lock) {
        super(name);
        this.read = read;
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public void read(){
        System.out.println(Thread.currentThread().getName() + " to lock read");
        readLock.lock();
        try {
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName() + " is reading...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
            System.out.println(Thread.currentThread().getName() + " unlock read successfully");
        }
    }

    public void write() {
        System.out.println(Thread.currentThread().getName() + " to lock write");
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " is writing...");
        } finally {
            writeLock.unlock();
            System.out.println(Thread.currentThread().getName() + " unlock write successfully");
        }
    }

    @Override
    public void run() {
        if (read) {
            read();
        } else {
            write();
        }
    }

}
