package src.rhino.metric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author zhanjun on 2017/4/23.
 */
public class HealthCountCollector {

    private static final HealthCountCollector INSTANCE = new HealthCountCollector();
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private static volatile Map<String, HealthCountSummary> summaryCounts = new ConcurrentHashMap<>();

    static {
        long initialDelay = 1000 - (System.currentTimeMillis() % 1000);
        scheduledExecutor.scheduleAtFixedRate(new CollectorTask(), initialDelay, 1000, TimeUnit.MILLISECONDS);
    }

    public static HealthCountCollector getInstance() {
        return INSTANCE;
    }

    public HealthCountSummary getHealthCountSummary(String key) {
        HealthCountSummary summaryHealthCount = summaryCounts.get(key);
        if (summaryHealthCount == null) {
            summaryHealthCount = new HealthCountSummary(0, 0, 0);
        }
        return summaryHealthCount;
    }

    private static class CollectorTask implements Runnable {
        @Override
        public void run() {
            Map<String, HealthCountBucket> healthCountBuckets = DefaultRhinoMetric.getHealthCountBuckets();
            int index = (int)(System.currentTimeMillis() / 1000) % 100;
            Map<String, HealthCountSummary> summaryCounts0 = new ConcurrentHashMap<>();

            for (String key : healthCountBuckets.keySet()) {
                HealthCountBucket healthCountBucket = healthCountBuckets.get(key);
                int rollingStatsTime = healthCountBucket.getRollingStatsTime();
                long success = 0, fail = 0;

                /*累计前 checkTime 秒的数据*/
                for (int i = 1; i <= rollingStatsTime; i++) {
                    int preIndex = index - i;
                    preIndex = preIndex >= 0 ? preIndex : preIndex + 100;
                    HealthCount healthCount = healthCountBucket.get(preIndex);
                    success += healthCount.getSuccessCount();
                    fail += healthCount.getErrorCount();
                }
                summaryCounts0.put(key, new HealthCountSummary(success, fail, (int)(success + fail) / rollingStatsTime));

                /*清空之前的数据*/
                for (int i = rollingStatsTime + 1, j = rollingStatsTime + 2; i < j; i++) {
                    int preIndex = index - i;
                    preIndex = preIndex >= 0 ? preIndex : preIndex + 100;
                    HealthCount healthCount0 = healthCountBucket.get(preIndex);
                    healthCount0.clear();
                }
            }
            summaryCounts = summaryCounts0;
        }
    }
}
