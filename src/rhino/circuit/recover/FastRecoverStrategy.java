package src.rhino.circuit.recover;


import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2017/6/16.
 */
public class FastRecoverStrategy extends AbstractRecoverStrategy {

    public FastRecoverStrategy(CircuitBreakerProperties properties) {
        super(properties);
    }

    @Override
    public long getPercent() {
        long remainingTime = getRemainingTime();
        if (remainingTime <= 0) {
            return 0;
        }
        return 1 << remainingTime;
    }
}
