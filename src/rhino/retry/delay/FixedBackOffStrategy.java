package src.rhino.retry.delay;

import src.rhino.retry.RetryContext;
import src.rhino.retry.RetryProperties;

/**
 * Created by zhen on 2019/2/20.
 */
public class FixedBackOffStrategy extends AbstractBackOffStrategy {

    private long delay;

    public FixedBackOffStrategy(RetryProperties retryProperties) {
        this.delay = retryProperties.getDelay();
    }

    @Override
    long getSleepDelay(RetryContext retryContext) {
        return delay;
    }
}
