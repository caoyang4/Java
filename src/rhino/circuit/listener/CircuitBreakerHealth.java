package src.rhino.circuit.listener;

import java.util.Map;

/**
 * Created by zhanjun on 2018/3/3.
 */
public class CircuitBreakerHealth {

    private long totalCount;
    private long errorCount;
    private float errorPercentage;
    private Map<String, Integer> exceptions;

    public CircuitBreakerHealth(long totalCount, long errorCount, float errorPercentage, Map<String, Integer> exceptions) {
        this.totalCount = totalCount;
        this.errorCount = errorCount;
        this.errorPercentage = errorPercentage;
        this.exceptions = exceptions;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public float getErrorPercentage() {
        return errorPercentage;
    }

    public Map<String, Integer> getExceptions() {
        return exceptions;
    }
}
