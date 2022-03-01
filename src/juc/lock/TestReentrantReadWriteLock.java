package src.juc.lock;

import com.sun.istack.internal.NotNull;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author caoyang
 */
public class TestReentrantReadWriteLock {
    public static int value;
    static ReentrantLock lock = new ReentrantLock();
    static ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    static Lock readLock = readWriteLock.readLock();
    static Lock writeLock = readWriteLock.writeLock();

    public static void main(String[] args) {
        for (int i=0; i<18; i++){
            new Thread(() -> {
                // ReentrantLock
                // read(lock);
                // 读锁
                System.out.println("first read");
                read(readLock);
            }).start();
        }

        for (int i=0; i < 5; i++){
            new Thread(() -> {
                // ReentrantLock
                // write(lock, new Random().nextInt());
                // 写锁
                write(writeLock, new Random().nextInt(100));
            }).start();
        }

        for (int i=0; i<10; i++){
            new Thread(() -> {
                // ReentrantLock
                // read(lock);
                // 读锁
                System.out.println("second read");
                read(readLock);
            }).start();
        }
    }

    public static void read(@NotNull Lock lock){
        lock.lock();
        try {
            Thread.sleep(1000);
            System.out.println("read over..., value="+value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public static void write(@NotNull Lock lock, int v){
        lock.lock();
        try {
            Thread.sleep(1000);
            value = v;
            System.out.println("write over..., value="+value);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
