package src.rhino.limit.strategy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import src.rhino.cache.RedisProperties;
import src.rhino.limit.feature.LimiterStrategyParams;
import src.rhino.limit.ratelimiter.ClusterRateLimiterFactory;
import src.rhino.limit.ratelimiter.RateLimiter;

/**
 * @author zhanjun on 2017/8/13.
 */
public class LimiterFrequencyClusterStrategy extends LimiterFrequencyStrategy {

    public static final LimiterStrategy INSTANCE = new LimiterFrequencyClusterStrategy(null);
    private static ConcurrentHashMap<String, RateLimiter> rateLimiterHolder = new ConcurrentHashMap<>();
    private static boolean isUseRedis = ClusterRateLimiterFactory.useRedis;
    private RedisProperties redisProperties;

    public LimiterFrequencyClusterStrategy(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

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
            rateLimiter = ClusterRateLimiterFactory.create(id, count, periodSecond, timeUnit, redisProperties);
            RateLimiter rateLimiter0 = rateLimiterHolder.putIfAbsent(id, rateLimiter);
            if (rateLimiter0 != null) {
                rateLimiter = rateLimiter0;
            }
        }

        //这里需要区分使用redis还是client实现集群限流，因为需要设置的参数不一致
        if (isUseRedis) {
            rateLimiter.setRate(count);
            rateLimiter.setPeriod(periodSecond);
        } else {
            rateLimiter.setRate((double)count / periodSecond);
        }
        return rateLimiter.tryAcquire(permits);
    }
}
