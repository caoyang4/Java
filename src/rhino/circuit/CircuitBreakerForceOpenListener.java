package src.rhino.circuit;

import src.rhino.circuit.listener.CircuitBreakerListenerTrigger;
import src.rhino.config.PropertyChangedListener;

/**
 * Created by zhanjun on 2019/1/26.
 */
public class CircuitBreakerForceOpenListener extends PropertyChangedListener<CircuitBreakerPropertyData> {

    private CircuitBreakerListenerTrigger listenerTrigger;
    private CircuitBreakerStatistic circuitBreakerStatistic;


    public CircuitBreakerForceOpenListener(String key, CircuitBreakerProperties circuitBreakerProperties,
                                           CircuitBreakerStatistic circuitBreakerStatistic,
                                           CircuitBreakerListenerTrigger listenerTrigger) {
        this.listenerTrigger = listenerTrigger;
        this.circuitBreakerStatistic = circuitBreakerStatistic;
        circuitBreakerProperties.addPropertyChangedListener(this);
    }

    @Override
    public void trigger(CircuitBreakerPropertyData oldProperty, CircuitBreakerPropertyData newProperty) {
        boolean oldForceOpen = oldProperty.getForceOpen();
        boolean newForceOpen = newProperty.getForceOpen();
        if (oldForceOpen == newForceOpen) {
            return;
        }

        // 强制熔断开启
        if (newForceOpen) {
            circuitBreakerStatistic.markOpened(true);
            listenerTrigger.circuitBreakerOpened(CircuitBreakerEventType.CIRCUIT_BREAKER_FORCE_OPEN, null);
        } else {
        // 强制熔断关闭
            circuitBreakerStatistic.markClosed(true);
            long duration = circuitBreakerStatistic.getCircuitDurationInSecond(true);
            long count = circuitBreakerStatistic.getDegradeCount(true);
            // 熔断关闭，主要关注熔断时长、熔断请求数
            CircuitBreakerEventData eventData = new CircuitBreakerEventData(duration, count);
            listenerTrigger.circuitBreakerClosed(CircuitBreakerEventType.CIRCUIT_BREAKER_FORCE_CLOSE, eventData);
        }
    }
}
