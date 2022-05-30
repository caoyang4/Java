package src.rhino.retry.delay;

import src.rhino.retry.RetryContext;
import src.rhino.retry.RetryProperties;

/**
 * Created by zhen on 2019/2/22.
 */
public class NoOpBackOffStrategy extends AbstractBackOffStrategy {

    public NoOpBackOffStrategy(RetryProperties retryProperties) {
    }

    @Override
    long getSleepDelay(RetryContext retryContext) {
        return 0;
    }
}
