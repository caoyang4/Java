package src.rhino.circuit.recover;

import java.util.concurrent.ThreadLocalRandom;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.RequestStatus;

/**
 * @author zhanjun on 2017/6/14.
 */
public abstract class AbstractRecoverStrategy implements RecoverStrategy {

    protected CircuitBreakerProperties properties;
    protected volatile long current;

    public AbstractRecoverStrategy(CircuitBreakerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void reset() {
        this.current = (System.currentTimeMillis() / 1000) - 1;
    }

    public long getRemainingTime() {
        return System.currentTimeMillis() / 1000 - current - properties.getRecoverDelayInSeconds();
    }

    /**
     * @param percentage
     * @return
     */
    @Override
    public RequestStatus doRecover(long percentage) {
        if (percentage <= 0) {
            return RequestStatus.DEGRADE;
        } else if (percentage >= 100) {
            return RequestStatus.NORMAL;
        }
        return ThreadLocalRandom.current().nextInt(100) > percentage ?
                RequestStatus.DEGRADE : RequestStatus.NORMAL;
    }
}
