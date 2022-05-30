package src.rhino.circuit.recover;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2017/6/16.
 */
public class TimeRecoverStrategy extends AbstractRecoverStrategy {

    public TimeRecoverStrategy(CircuitBreakerProperties properties) {
        super(properties);
    }

    @Override
    public long getPercent() {
        long remainingTime = getRemainingTime();
        return remainingTime <= 0 ? 0 : remainingTime * 100 / properties.getRecoverTimeInSeconds();
    }
}
