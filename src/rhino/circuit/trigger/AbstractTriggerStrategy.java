package src.rhino.circuit.trigger;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.metric.HealthCountSummary;

/**
 * @author zhanjun on 2017/6/15.
 */
public abstract class AbstractTriggerStrategy implements TriggerStrategy {

    protected CircuitBreakerProperties properties;

    public AbstractTriggerStrategy(CircuitBreakerProperties properties) {
        this.properties = properties;
    }

    /**
     * circuit breaker trigger condition
     * @param health
     * @return
     */
    @Override
    public abstract boolean trigger(HealthCountSummary health);
}
