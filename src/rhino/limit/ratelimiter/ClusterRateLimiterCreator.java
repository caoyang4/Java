package src.rhino.limit.ratelimiter;

import java.util.concurrent.TimeUnit;

import src.rhino.cache.RedisProperties;

/**
 * @author zhanjun on 2017/8/13.
 */
public interface ClusterRateLimiterCreator {

    /**
     * create cluster rate limiter
     * @param id
     * @param count
     * @param period
     * @return
     */
    RateLimiter create(String id, int count, long period, TimeUnit timeUnit, RedisProperties redisProperties);
}
