package src.rhino.retry.delay;

import src.rhino.retry.RetryContext;
import src.rhino.retry.RetryProperties;

/**
 * Created by zhen on 2019/2/20.
 */
public class RandomRangeBackOffStrategy extends AbstractBackOffStrategy {

    private long minDelay;
    private long maxDelay;

    public RandomRangeBackOffStrategy(RetryProperties retryProperties) {
        maxDelay = retryProperties.getMaxDelay();
        minDelay = retryProperties.getMinDelay();
    }

    @Override
    long getSleepDelay(RetryContext retryContext) {
        return minDelay + (long) (Math.random() * (maxDelay - minDelay));
    }
}
