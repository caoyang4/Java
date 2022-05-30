package src.rhino.limit.ratelimiter;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanjun on 2017/8/13.
 */
public abstract class AbstractRateLimiter implements RateLimiter {

	private static final String ERROR_MSG = "AbstractRateLimiter is just a stub and cannot be used as complete implementation of RequestLimiter";

	@Override
	public boolean tryAcquire() {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public boolean tryAcquire(int permits) {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public double acquire() {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public double acquire(int permits) {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public void setRate(double rate) {
		throw new UnsupportedOperationException(ERROR_MSG);
	}

	@Override
	public void setPeriod(long period) {
		throw new UnsupportedOperationException(ERROR_MSG);
	}
	
	@Override
	public long acquireNoWait(int permits) {
		throw new UnsupportedOperationException(ERROR_MSG);
	}
}
