package src.rhino.circuit.degrade;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * Created by zhanjun on 2018/3/29.
 */
public class DefaultDegradeStrategy extends AbstractDegradeStrategy {

    public DefaultDegradeStrategy(CircuitBreakerProperties properties) {
        super(properties);
    }

    @Override
    public Object degrade() {
        return null;
    }

    @Override
    public boolean isDefault() {
        return true;
    }
}
