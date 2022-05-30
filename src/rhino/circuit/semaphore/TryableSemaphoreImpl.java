package src.rhino.circuit.semaphore;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhanjun on 2018/4/3.
 */
public class TryableSemaphoreImpl implements TryableSemaphore {

    private volatile int permits;
    private final AtomicInteger count = new AtomicInteger(0);

    public TryableSemaphoreImpl(int permits) {
        this.permits = permits;
    }

    @Override
    public boolean tryAcquire() {
        if (permits == 0) return true;
        int currentCount = count.incrementAndGet();
        if (currentCount > permits) {
            count.decrementAndGet();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setPermits(int permits) {
        this.permits = permits;
    }

    @Override
    public void release() {
        if (permits > 0) {
            count.decrementAndGet();
        }
    }

    @Override
    public int getNumberOfPermitsUsed() {
        return count.get();
    }
}
