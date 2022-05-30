package src.rhino.metric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * @author zhanjun on 2017/4/21.
 */
public class DefaultRhinoMetric {

    private static Map<String, HealthCountBucket> healthCountBuckets = new ConcurrentHashMap<>();
    private static HealthCountCollector healCountCollector = HealthCountCollector.getInstance();
    private String key;
    private HealthCountBucket healthCountBucket;

    public DefaultRhinoMetric(String key, CircuitBreakerProperties properties) {
        this.key = key;
        this.healthCountBucket = new HealthCountBucket(properties);
        healthCountBuckets.put(key, healthCountBucket);
    }

    /**
     * mark fail
     * @param throwable
     */
    public void markFailed(Throwable throwable) {
        healthCountBucket.mark(throwable);
    }

    /**
     * mark success
     */
    public void markSuccess() {
        healthCountBucket.mark(null);
    }

    /**
     * 清空数据
     */
    public void reset() {
        healCountCollector.getHealthCountSummary(key).clear();
        healthCountBucket.reset();
    }

    /**
     * 熔断之后计算异常统计数据
     * @return
     */
    public Map<String, Count> getExceptionSummary() {
        int index = (int)(System.currentTimeMillis() / 1000) % 100;
        int rollingStatsTime = healthCountBucket.getRollingStatsTime();
        /*汇总rollingStatsTime内的异常情况*/
        Map<String, Count> exceptionCountSummary = new HashMap<>();
        for (int i = 0; i <= rollingStatsTime; i++) {
            int preIndex = index - i;
            preIndex = preIndex >= 0 ? preIndex : preIndex + 100;
            HealthCount healthCount = healthCountBucket.get(preIndex);
            Map<Class<? extends Throwable>, AtomicInteger> exceptionCount = healthCount.getExceptionCount();
            for (Class exception : exceptionCount.keySet()) {
                String className = exception.getCanonicalName();
                Count summary = exceptionCountSummary.get(className);
                if (summary == null) {
                    summary = new Count();
                    exceptionCountSummary.put(className, summary);
                }
                summary.add(exceptionCount.get(exception).get());
            }
        }
        return exceptionCountSummary;
    }

    /**
     * 获取当前和统计时间区间的请求统计数据
     * @return
     */
    public HealthCountSummary getHealthCount() {
        HealthCountSummary summaryHealthCount = healCountCollector.getHealthCountSummary(key);
        int index = (int)(System.currentTimeMillis() / 1000) % 100;
        HealthCount latestHealthCount = healthCountBucket.get(index);
        HealthCountSummary healthCountSummary = summaryHealthCount.plus(latestHealthCount);
        return healthCountSummary;
    }

    public static Map<String, HealthCountBucket> getHealthCountBuckets() {
        return healthCountBuckets;
    }
}
