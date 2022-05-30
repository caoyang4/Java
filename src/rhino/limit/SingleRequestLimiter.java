package src.rhino.limit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import src.rhino.dispatcher.RhinoEvent;
import src.rhino.exception.RhinoLimitRejectException;
import src.rhino.limit.ratelimiter.RateLimiter;

/**
 * @author zhanjun on 2017/4/27.
 */
public class SingleRequestLimiter extends AbstractRequestLimiter {

    private RateLimiter rateLimiter;
    private RequestLimiterProperties properties;
    private AtomicInteger rateHolder;

    /**
     * @param key
     * @param properties
     */
    public SingleRequestLimiter(String key, RequestLimiterProperties properties) {
        super(key);
        this.properties = properties;
        this.rateHolder = new AtomicInteger(properties.getRate());
        this.rateLimiter = RateLimiter.Factory.createSmoothRateLimiter(rateHolder.get());
    }

    @Override
    public boolean tryAcquire() {
        if (!properties.getIsActive()) {
            return true;
        }
        int oldRate = rateHolder.get();
        int newRate = properties.getRate();
        if (newRate != 0) {
            if (newRate != oldRate && rateHolder.compareAndSet(oldRate, newRate)) {
                rateLimiter.setRate(newRate);
            }
            LimiterHandlerEnum strategy = LimiterHandlerEnum.getStrategy(properties.getStrategy());
            long timeout = 0;
            if (strategy.isWait()) {
                timeout = properties.getTimeoutInMilliseconds();
            }

            if (rateLimiter.tryAcquire(1, timeout, TimeUnit.MILLISECONDS)) {
                eventDispatcher.dispatchEvent(new RhinoEvent(LimiterEventType.ACCESS));
                return true;
            }
        }
        eventDispatcher.dispatchEvent(new RhinoEvent(LimiterEventType.REFUSE));
        return false;
    }

    @Override
    public boolean isNeedDegrade() {
        if (tryAcquire()) {
            return false;
        } else {
            LimiterHandlerEnum strategy = LimiterHandlerEnum.getStrategy(properties.getStrategy());
            if (strategy.isDegrade()) {
                eventDispatcher.dispatchEvent(new RhinoEvent(LimiterEventType.DEGRADE));
                return true;
            }
            throw new RhinoLimitRejectException();
        }
    }
}
