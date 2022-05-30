package src.rhino.retry.delay;

import java.util.concurrent.TimeUnit;

import src.rhino.exception.RhinoRetryInterruptedException;
import src.rhino.retry.RetryContext;

/**
 * Created by zhen on 2019/2/22.
 */
public abstract class AbstractBackOffStrategy implements BackOffStrategy {

    @Override
    public void backOff(RetryContext retryContext) {
        long sleepDelay = getSleepDelay(retryContext);
        retryContext.registerSleepDelay(sleepDelay);
        if (sleepDelay > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(sleepDelay);
            } catch (InterruptedException e) {
                throw new RhinoRetryInterruptedException(e.getMessage(), e);
            }
        }
    }

    abstract long getSleepDelay(RetryContext retryContext);
}
