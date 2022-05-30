package src.rhino.limit.strategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import src.rhino.limit.feature.LimiterStrategyParams;
import src.rhino.limit.ratelimiter.ClientRateLimiter;
import src.rhino.limit.ratelimiter.RateLimiter;

/**
 * @author zhanjun on 2017/8/13.
 */
public class LimiterFrequencyClientStrategy extends LimiterFrequencyStrategy {

    public static final LimiterStrategy INSTANCE = new LimiterFrequencyClientStrategy();
    private static ConcurrentHashMap<String, RateLimiter> rateLimiterHolder = new ConcurrentHashMap<>();

    @Override
    public boolean doLimit(String id, int permits, LimiterStrategyParams strategyParams) {
        TimeUnit timeUnit = strategyParams.getTimeUnit();
        int duration = strategyParams.getDuration();
        int count = strategyParams.getCount();
        long periodSecond = duration;
        if (timeUnit != null) {
            periodSecond = timeUnit.toSeconds(duration);
        }
        if (count <= 0) {
            return false;
        }

        if (periodSecond <= 0) {
            return true;
        }

        RateLimiter rateLimiter = rateLimiterHolder.get(id);
        if (rateLimiter == null) {
            rateLimiter = new ClientRateLimiter(count, periodSecond);
            RateLimiter rateLimiter0 = rateLimiterHolder.putIfAbsent(id, rateLimiter);
            if (rateLimiter0 != null) {
                rateLimiter = rateLimiter0;
            }
        }
        rateLimiter.setRate((double)count / periodSecond);
        return rateLimiter.tryAcquire(permits);
    }
}
