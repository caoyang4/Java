package src.rhino.limit.ratelimiter;

import java.util.concurrent.TimeUnit;

/**
 * @author zhanjun on 2017/7/3.
 */
public interface RateLimiter {

    /**
     * try access
     *
     * @return
     */
    boolean tryAcquire();

    /**
     * try access with permits
     *
     * @param permits
     * @return
     */
    boolean tryAcquire(int permits);

    /**
     * try access with permits and timeout
     *
     * @param permits
     * @param timeout
     * @param unit
     * @return
     */
    boolean tryAcquire(int permits, long timeout, TimeUnit unit);

    /**
     * default acquire 1 permit,wait until get
     *
     * @return wait time,milliseconds
     */
    double acquire();

    /**
     * acquire permits ,wait until get
     *
     * @param permits
     * @return wait time , milliseconds
     */
    double acquire(int permits);

    /**
     * acquire permits ,get no wait
     *
     * @param permits
     * @return
     */
    long acquireNoWait(int permits);

    /**
     * set rate
     *
     * @param rate
     */
    void setRate(double rate);

    /**
     * set period
     *
     * @param period
     */
    void setPeriod(long period);

    class Factory {

        public static RateLimiter createSmoothRateLimiter(double rate) {
            return GuavaRateLimiter.create(rate);
        }
    }
}
