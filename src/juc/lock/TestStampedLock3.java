package src.juc.lock;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.locks.StampedLock;

/**
 * @author caoyang
 * @create 2022-06-07 23:31
 */
@Slf4j(topic = "TestStampedLock3")
public class TestStampedLock3 {
    public static void main(String[] args) {
        final StampedLock lock = new StampedLock();
        new Thread(() -> {
            long l1 = lock.writeLock();
            try {
                // 不可重入，若线程已经持有了写锁，再去尝试获取写锁就会造成死锁
                log.info("get writeLock: " + l1);
                long l2 = lock.writeLock();
                try{
                    log.info("get writeLock: " + l2);
                } finally {
                    lock.unlockWrite(l2);
                }
            } finally {
                lock.unlockWrite(l1);
            }
        }, "thread1").start();

    }
}
