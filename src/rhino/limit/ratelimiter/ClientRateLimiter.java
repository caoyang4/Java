package src.rhino.limit.ratelimiter;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * @author zhanjun on 2017/8/13.
 */
public class ClientRateLimiter implements RateLimiter {

    private AtomicDouble permitsPerSecond;
    private volatile RateLimiter rateLimiter;

    public ClientRateLimiter(int count, long period) {
        this.permitsPerSecond = new AtomicDouble((double) count / period);
        this.rateLimiter = RateLimiter.Factory.createSmoothRateLimiter(permitsPerSecond.get());
    }

    @Override
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    @Override
    public boolean tryAcquire(int permits) {
        return rateLimiter.tryAcquire(permits);
    }

    @Override
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        return rateLimiter.tryAcquire(permits, timeout, unit);
    }
    
    @Override
    public double acquire() {
        return rateLimiter.acquire();
    }
    
    @Override
    public double acquire(int permits) {
        return rateLimiter.acquire(permits);
    }

    @Override
    public void setRate(double rate) {
        double oldValue = permitsPerSecond.get();
        if (Double.compare(oldValue, rate) != 0 && permitsPerSecond.compareAndSet(oldValue, rate)) {
            rateLimiter.setRate(rate);
        }
    }
    
    @Override
    public void setPeriod(long period) {
        //Do nothing
    }

	@Override
	public long acquireNoWait(int permits) {
		return rateLimiter.acquireNoWait(permits);
	}
}
