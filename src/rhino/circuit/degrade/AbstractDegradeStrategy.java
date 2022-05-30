package src.rhino.circuit.degrade;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2018/3/29.
 */
public abstract class AbstractDegradeStrategy implements DegradeStrategy {

    protected CircuitBreakerProperties properties;
    protected String value;

    public AbstractDegradeStrategy(CircuitBreakerProperties properties) {
        this.properties = properties;
        this.value = properties.getDegradeStrategyValue();
    }

    @Override
    public boolean isDefault() {
        return false;
    }
}
