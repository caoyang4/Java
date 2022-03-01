package src.juc.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 锁降级：写锁降级成为读锁
 * 锁降级是把当前持有的写锁，再获取到读锁，随后释放(先前拥有的)写锁的过程
 *
 * @author caoyang
 */
public class TestReentrantReadWriteLock3 {
    public static void main(String[] args) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        List<TestReentrantReadWriteLock3Thread> threadList = new ArrayList<>();
        A obj = new A(false);
        for (int i = 0; i < 5; i++) {
            TestReentrantReadWriteLock3Thread thread = new TestReentrantReadWriteLock3Thread("Thread"+i, obj, lock);
            threadList.add(thread);
        }
        for (TestReentrantReadWriteLock3Thread thread : threadList) {
            thread.start();
        }

    }
}

class TestReentrantReadWriteLock3Thread extends Thread{
    Lock readLock;
    Lock writeLock;
    A obj;

    public TestReentrantReadWriteLock3Thread(String name, A obj, ReentrantReadWriteLock lock) {
        super(name);
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        this.obj = obj;
    }

    public void process(){
        readLock.lock();
        try {
            if(!obj.update){
                // 释放读锁
                readLock.unlock();
                writeLock.lock();
                try {
                    if(!obj.update){
                        System.out.println(Thread.currentThread().getName() + " has done updating");
                        obj.update = true;
                    }
                    // 修改完之后，再获取读锁
                    readLock.lock();
                } finally {
                    writeLock.unlock();
                }
            } else {
                System.out.println(Thread.currentThread().getName() + " knows that it happens updating");
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void run() {
        process();
    }
}

class A{
    boolean update;
    public A(boolean update) {
        this.update = update;
    }
}
