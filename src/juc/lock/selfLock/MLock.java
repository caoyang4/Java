package src.juc.lock.selfLock;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义锁，实现 Lock 接口
 * @author caoyang
 */
@Slf4j(topic = "MLock")
public class MLock implements Lock {
    /*
    public static void main(String[] args) {
        Lock lock = new MLock();
        lock.lock();
        try {
            log.info("get lock");
            lock.lock();
            try {
                log.info("get lock again");
            } finally {
                lock.unlock();
            }
        } finally {
            lock.unlock();
        }
    }
    */

    private volatile int symbol = 0;
    @Override
    public void lock() {
        synchronized (this) {
            // 不可重入锁
            while (symbol != 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            symbol = 1;
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        synchronized (this) {
            symbol = 0;
            notifyAll();
        }
    }

    @NotNull
    @Override
    public Condition newCondition() {
        return null;
    }
}
