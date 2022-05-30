package src.rhino.retry.delay;

import src.rhino.retry.RetryContext;
import src.rhino.retry.RetryProperties;

/**
 * Created by zhen on 2019/2/22.
 */
public class ExponentialBackOffStrategy extends AbstractBackOffStrategy {

    private long delay;
    private long maxDelay;
    private double multiplier;


    public ExponentialBackOffStrategy(RetryProperties retryProperties) {
        this.delay = retryProperties.getDelay();
        this.maxDelay = retryProperties.getMaxDelay();
        this.multiplier = retryProperties.getMultiplier();
    }

    protected double getMultiplier() {
        return multiplier;
    }

    @Override
    long getSleepDelay(RetryContext retryContext) {
        int attempt = retryContext.getAttempt();
        long sleepDelay = delay;
        for (int i = 1; i < attempt; i++) {
            sleepDelay *= multiplier;
        }
        return sleepDelay > maxDelay ? maxDelay : sleepDelay;
    }
}
