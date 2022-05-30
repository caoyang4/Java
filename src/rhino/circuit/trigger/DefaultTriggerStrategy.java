package src.rhino.circuit.trigger;

import java.util.Calendar;
import java.util.List;

import org.springframework.util.CollectionUtils;

import src.rhino.circuit.CircuitBreakerProperties;
import src.rhino.circuit.CircuitBreakerTriggerRangeData;
import src.rhino.metric.HealthCountSummary;

/**
 * @author zhanjun on 2017/6/15.
 */
public class DefaultTriggerStrategy extends AbstractTriggerStrategy {

    public DefaultTriggerStrategy(CircuitBreakerProperties properties) {
        super(properties);
    }

    @Override
    public boolean trigger(HealthCountSummary health) {
        System.out.println("Rhino.CircuitBreaker.health " + health.toJson());

        List<CircuitBreakerTriggerRangeData> rangeDataList = properties.getCircuitBreakerTriggerRangeDataList();
        if (!CollectionUtils.isEmpty(rangeDataList)) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            for (CircuitBreakerTriggerRangeData rangeData : rangeDataList) {
                if (rangeData.isMatch(hour, minute)) {
                    return health.getTotalRequests() >= rangeData.getRequestVolumeThreshold() &&
                            health.getErrorPercentage() >= rangeData.getErrorThresholdPercentage() &&
                            health.getErrorCount() >= rangeData.getErrorThresholdCount();
                }
            }
        }
        return health.getTotalRequests() >= properties.getRequestVolumeThreshold() &&
                health.getErrorPercentage() >= properties.getErrorThresholdPercentage() &&
                health.getErrorCount() >= properties.getErrorThresholdCount();
    }
}
