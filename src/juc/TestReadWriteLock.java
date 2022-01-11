package juc;

import com.sun.istack.internal.NotNull;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author caoyang
 */
public class TestReadWriteLock {
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
                read(readLock);
            }).start();
        }

        for (int i=0; i<2; i++){
            new Thread(() -> {
                // ReentrantLock
                // write(lock, new Random().nextInt());
                // 写锁
                write(writeLock, new Random().nextInt());
            }).start();
        }

    }

    public static void read(@NotNull Lock lock){
        lock.lock();
        try {
            Thread.sleep(1000);
            System.out.println("read over...");
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
            System.out.println("write over...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
