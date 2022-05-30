package src.rhino.limit;

import src.rhino.cache.RedisProperties;
import src.rhino.limit.feature.Features;

/**
 * Created by zhanjun on 2017/4/24.
 */
public class NoOpRequestLimiter implements RequestLimiter {

    @Override
    public boolean tryAcquire() {
        return true;
    }

    @Override
    public boolean tryAcquire(String key) {
        return true;
    }

    @Override
    public boolean tryAcquire(Features features) {
        return true;
    }

    @Override
    public boolean tryAcquire(Features features, int permits) {
        return true;
    }

    @Override
    public boolean isNeedDegrade() {
        return false;
    }

    @Override
    public void setRedisProperties(RedisProperties redisProperties) {
        //Do nothing

    }
}
