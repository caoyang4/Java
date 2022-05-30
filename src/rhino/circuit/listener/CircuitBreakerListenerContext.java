
package src.rhino.circuit.listener;

import java.util.HashMap;
import java.util.Map;

import src.rhino.circuit.CircuitBreakerEventData;
import src.rhino.circuit.CircuitBreakerEventType;
import src.rhino.metric.Count;
import src.rhino.metric.HealthCountSummary;

/**
 * Created by wanghao on 18/3/2.
 */
public class CircuitBreakerListenerContext {

    /**
     * rhinoKey
     */
    private String rhinoKey;

    /**
     * 熔断事件
     */
    private CircuitBreakerEventType eventType;


    private CircuitBreakerEventData eventData;

    /**
     * 熔断触发和恢复时的接口健康数据，对外数据结构
     */
    private CircuitBreakerHealth circuitBreakerHealth;

    CircuitBreakerListenerContext(String rhinoKey, CircuitBreakerEventType eventType, CircuitBreakerEventData eventData) {
        this.rhinoKey = rhinoKey;
        this.eventType = eventType;
        this.eventData = eventData;
    }

    public String getRhinoKey() {
        return rhinoKey;
    }

    public CircuitBreakerEventType getEventType() {
        return eventType;
    }

    public CircuitBreakerHealth getCircuitBreakerHealth() {
        if (circuitBreakerHealth == null) {
            circuitBreakerHealth = genCircuitBreakerHealth(eventData);
        }
        return circuitBreakerHealth;
    }

    private CircuitBreakerHealth genCircuitBreakerHealth(CircuitBreakerEventData eventData) {
        if (eventData == null) {
            return null;
        }

        HealthCountSummary healthCountSummary = eventData.getHealthCountSummary();

        long totalCount = 0;
        long errorCount = 0;
        float errorPercentage = 0;
        if (healthCountSummary != null) {
            totalCount = healthCountSummary.getTotalRequests();
            errorCount = healthCountSummary.getErrorCount();
            errorPercentage = healthCountSummary.getErrorPercentage();
        }

        Map<String, Integer> exceptions = new HashMap<>();
        Map<String, Count> exceptionSummary = eventData.getExceptionSummary();
        if (exceptionSummary != null) {
            for (String key : exceptionSummary.keySet()) {
                exceptions.put(key, exceptionSummary.get(key).getValue());
            }
        }
        return new CircuitBreakerHealth(totalCount, errorCount, errorPercentage, exceptions);
    }
}
