package src.juc.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock的操作都转化为对Sync对象的操作，
 * 由于Sync继承了AQS，所以基本上都可以转化为对AQS的操作
 *
 * @author caoyang
 */
public class TestReentrantLock1 {
    public static void main(String[] args) {
        Lock lock = new ReentrantLock(true);
        for (int i = 0; i < 100; i++) {
            TestReentrantLockThread test = new TestReentrantLockThread("TreadRL"+i, lock);
            test.start();
        }
    }

}

class TestReentrantLockThread extends Thread{
    Lock lock;

    public TestReentrantLockThread(String name, Lock lock) {
        super(name);
        this.lock = lock;
    }

    @Override
    public void run() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " running...");
        } finally {
            lock.unlock();
        }
    }
}


