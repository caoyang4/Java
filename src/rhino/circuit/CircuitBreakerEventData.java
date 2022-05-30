package src.rhino.circuit;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import src.rhino.dispatcher.RhinoEventData;
import src.rhino.metric.Count;
import src.rhino.metric.HealthCountSummary;

/**
 * @author zhanjun
 */
public class CircuitBreakerEventData implements RhinoEventData {

    private HealthCountSummary healthCountSummary;
    private Map<String, Count> exceptionSummary;
    private long durationInSecond;
    private long degradeCount;
    private static final String EXCEPTION_COUNT_KEY = "rhino.circuitbreaker.exception.count";

    public CircuitBreakerEventData(HealthCountSummary healthCountSummary, Map<String, Count> exceptionSummary) {
        this.healthCountSummary = healthCountSummary;
        this.exceptionSummary = exceptionSummary;
    }

    public CircuitBreakerEventData(long durationInSecond, long degradeCount) {
        this.durationInSecond = durationInSecond;
        this.degradeCount = degradeCount;
    }

    public CircuitBreakerEventData(HealthCountSummary healthCountSummary, Map<String, Count> exceptionSummary, long durationInSecond, long degradeCount) {
        this.healthCountSummary = healthCountSummary;
        this.exceptionSummary = exceptionSummary;
        this.durationInSecond = durationInSecond;
        this.degradeCount = degradeCount;
    }

    public HealthCountSummary getHealthCountSummary() {
        return healthCountSummary;
    }

    public Map<String, Count> getExceptionSummary() {
        return exceptionSummary;
    }

    @Override
    public String toJson() {
        StringBuilder builder = new StringBuilder("{");
        if (healthCountSummary != null) {
            builder.append("\"healthCount\":");
            builder.append(healthCountSummary.toJson());
            builder.append(",");
        }
        builder.append("\"durationInSecond\":");
        builder.append(durationInSecond);
        builder.append(",");
        builder.append("\"degradeCount\":");
        builder.append(degradeCount);
        if (exceptionSummary != null) {
            builder.append(",");
            builder.append("\"exception\":");
            builder.append("{");
            Map<String, Count> selectedException = selectException();
            //处理异常统计为空的情况（hystrix）
            if (selectedException.size() != 0) {
                for (String key : selectedException.keySet()) {
                    builder.append("\"");
                    builder.append(key);
                    builder.append("\"");
                    builder.append(":");
                    builder.append(selectedException.get(key).getValue());
                    builder.append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
            }
            builder.append("}");
        }
        builder.append("}");
        return builder.toString();
    }

    private Map<String, Count> selectException() {
        int count = 3;
        if (exceptionSummary.size() <= count) {
            return exceptionSummary;
        }
        List<Map.Entry<String, Count>> list = new LinkedList<>(exceptionSummary.entrySet());
        Collections.sort(list, (o1, o2) -> o1.getValue().getValue() < o2.getValue().getValue() ? 1 : -1);

        Map<String, Count> result = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            Map.Entry<String, Count> entry = list.get(i);
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
