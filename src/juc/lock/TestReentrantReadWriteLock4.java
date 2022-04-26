package src.juc.lock;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 读写锁不能共存
 * @author caoyang
 */
@Slf4j(topic = "TestReentrantReadWriteLock4")
public class TestReentrantReadWriteLock4 {
    public static void main(String[] args) {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        Lock readLock = lock.readLock();
        Lock writeLock = lock.writeLock();
        Thread read = new Thread(() -> {
            readLock.lock();
            try {
                log.info("t1 get read lock");
                JucUtils.sleepSeconds(5);
            } finally {
                readLock.unlock();
                log.info("t1 unlock read");
            }
        });
        Thread write = new Thread(() -> {
            JucUtils.sleepSeconds(1);
            writeLock.lock();
            try {
                log.info("t2 get write lock");
            } finally {
                writeLock.unlock();
                log.info("t1 unlock write");
            }
        });
        read.start();
        write.start();
    }

}
