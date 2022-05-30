package src.rhino.limit;


import src.rhino.cache.RedisProperties;
import src.rhino.dispatcher.RhinoEventDispatcher;
import src.rhino.limit.feature.Features;

/**
 * @author  zhanjun on 2017/7/3.
 */
public abstract class AbstractRequestLimiter implements RequestLimiter {

    protected RhinoEventDispatcher eventDispatcher;

    private static final String ERROR_MSG = "AbstractRequestLimiter is just a stub and cannot be used as complete implementation of RequestLimiter";

    public AbstractRequestLimiter(String rhinoKey) {
        this.eventDispatcher = new LimiterEventDispatcher(rhinoKey);
    }

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
        throw new UnsupportedOperationException(ERROR_MSG);
    }

    @Override
    public void setRedisProperties(RedisProperties redisProperties) {

    }
}
