package src.rhino.retry.delay;

import src.rhino.retry.RetryContext;
import src.rhino.retry.RetryProperties;

/**
 * Created by zhen on 2019/2/25.
 */
public class ExponentialRandomBackOffStrategy extends ExponentialBackOffStrategy {

    public ExponentialRandomBackOffStrategy(RetryProperties retryProperties) {
        super(retryProperties);
    }

    @Override
    long getSleepDelay(RetryContext retryContext) {
        long next = super.getSleepDelay(retryContext);
        next = (long) (next * (1 + Math.random() * (getMultiplier() - 1)));
        return next;
    }
}
