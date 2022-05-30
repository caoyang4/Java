package src.rhino.metric;

import java.util.ArrayList;
import java.util.List;

import src.rhino.circuit.CircuitBreakerProperties;

/**
 * @author zhanjun on 2017/4/23.
 */
public class HealthCountBucket {

    private static final int NUM = 100;
    private HealthCount[] healthCounts = new HealthCount[NUM];
    private CircuitBreakerProperties properties;

    public HealthCountBucket(CircuitBreakerProperties properties) {
        this.properties = properties;
        for (int i = 0; i < NUM; i++) {
            healthCounts[i] = new HealthCount();
        }
    }

    /**
     * @param throwable
     */
    public void mark(Throwable throwable) {
        int index = (int)(System.currentTimeMillis() / 1000) % 100;
        HealthCount healthCount = healthCounts[index];
        if (throwable == null) {
            healthCount.markSuccess();
        } else {
            healthCount.markFailed(throwable);
        }
    }

    public void reset() {
        for (int i = 0; i < NUM; i++) {
            healthCounts[i].clear();
        }
    }

    public HealthCount get(int index) {
        return healthCounts[index];
    }

    public int getRollingStatsTime() {
        return properties.getRollingStatsTime();
    }

    public List<HealthCount> list(int n) {
        if (n > NUM) {
            n = NUM;
        }

        int index = (int) (System.currentTimeMillis() / 1000) % NUM;
        List<HealthCount> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            index = (index + NUM - 1) % NUM;
            list.add(get(index));
        }
        return list;
    }
}
