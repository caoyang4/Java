package src.juc.lock.selfLock;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义锁，实现 Lock 接口
 * @author caoyang
 */
public class MLock implements Lock {
    private volatile int symbol = 0;
    @Override
    public void lock() {
        synchronized (this) {
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
