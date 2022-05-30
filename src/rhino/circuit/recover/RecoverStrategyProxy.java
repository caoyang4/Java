package src.rhino.circuit.recover;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.CircuitBreakerPropertyData;
import src.rhino.circuit.RequestStatus;
import src.rhino.config.PropertyChangedListener;

/**
 * @author zhanjun on 2017/09/03.
 */
public class RecoverStrategyProxy extends PropertyChangedListener<CircuitBreakerPropertyData> implements RecoverStrategy {

    private volatile RecoverStrategy recoverStrategy;
    private CircuitBreakerProperties circuitBreakerProperties;

    public RecoverStrategyProxy(RecoverStrategy recoverStrategy, CircuitBreakerProperties circuitBreakerProperties) {
        this.recoverStrategy = recoverStrategy;
        this.circuitBreakerProperties = circuitBreakerProperties;
        circuitBreakerProperties.addPropertyChangedListener(this);
    }

    @Override
    public RequestStatus doRecover(long percentage) {
        return recoverStrategy.doRecover(percentage);
    }

    @Override
    public void reset() {
        recoverStrategy.reset();
    }

    @Override
    public long getPercent() {
        return recoverStrategy.getPercent();
    }

    @Override
    public void trigger(CircuitBreakerPropertyData oldProperty, CircuitBreakerPropertyData newProperty) {
        if (oldProperty.getRecoverStrategy() != newProperty.getRecoverStrategy()) {
            recoverStrategy = Type.get(newProperty.getRecoverStrategy()).create(circuitBreakerProperties);
        }
    }
}
