package src.rhino.circuit.trigger;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.CircuitBreakerPropertyData;
import src.rhino.config.PropertyChangedListener;
import src.rhino.metric.HealthCountSummary;

/**
 * @author zhanjun on 2017/09/03.
 */
public class TriggerStrategyProxy extends PropertyChangedListener<CircuitBreakerPropertyData> implements TriggerStrategy {

    private volatile TriggerStrategy triggerStrategy;
    private CircuitBreakerProperties circuitBreakerProperties;

    public TriggerStrategyProxy(TriggerStrategy triggerStrategy, CircuitBreakerProperties circuitBreakerProperties) {
        this.triggerStrategy = triggerStrategy;
        this.circuitBreakerProperties = circuitBreakerProperties;
    }

    @Override
    public boolean trigger(HealthCountSummary health) {
        return triggerStrategy.trigger(health);
    }

    @Override
    public void trigger(CircuitBreakerPropertyData oldProperty, CircuitBreakerPropertyData newProperty) {
        if (oldProperty.getTriggerStrategy() != newProperty.getTriggerStrategy()) {
            triggerStrategy = TriggerStrategy.Type.get(newProperty.getTriggerStrategy()).create(circuitBreakerProperties);
        }
    }
}
