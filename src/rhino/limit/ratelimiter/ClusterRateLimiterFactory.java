package src.rhino.limit.ratelimiter;

import java.util.concurrent.TimeUnit;

import src.rhino.cache.RedisProperties;
import src.rhino.util.ExtensionLoader;

/**
 * @author zhanjun on 2017/8/13.
 */
public class ClusterRateLimiterFactory {

    private static ClusterRateLimiterCreator creator = ExtensionLoader.getExtension(ClusterRateLimiterCreator.class);
    public static boolean useRedis = true;

    static {
        if (creator == null) {
            useRedis = false;
            creator = new ClusterRateLimiterCreator() {

                @Override
                public RateLimiter create(String id, int count, long period, TimeUnit timeUnit, RedisProperties redisProperties) {
                    return new ClientRateLimiter(count, period);
                }
            };
        }
    }

    /**
     *
     * @param id
     * @param count
     * @param period
     * @return
     */
    public static RateLimiter create(String id, int count, long period, TimeUnit timeUnit,  RedisProperties redisProperties) {
        return creator.create(id, count, period, timeUnit, redisProperties);
    }
}
