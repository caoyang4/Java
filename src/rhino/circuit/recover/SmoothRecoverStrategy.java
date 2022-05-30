package src.rhino.circuit.recover;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2017/6/15.
 */
public class SmoothRecoverStrategy extends AbstractRecoverStrategy {

    public SmoothRecoverStrategy(CircuitBreakerProperties properties) {
        super(properties);
    }

    @Override
    public long getPercent() {
        return getRemainingTime();
    }
}
