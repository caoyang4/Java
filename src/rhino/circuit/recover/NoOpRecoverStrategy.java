package src.rhino.circuit.recover;


import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2017/6/16.
 */
public class NoOpRecoverStrategy extends AbstractRecoverStrategy {

    public NoOpRecoverStrategy(CircuitBreakerProperties properties) {
        super(properties);
    }

    @Override
    public long getPercent() {
        long remainingTime = getRemainingTime();
        return remainingTime <= 0 ? 0 : 100;
    }
}
