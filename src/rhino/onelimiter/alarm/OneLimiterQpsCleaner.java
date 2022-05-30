package src.rhino.onelimiter.alarm;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * Created by zhen on 2018/11/29.
 */
public class OneLimiterQpsCleaner {

    private final static Logger logger = LoggerFactory.getLogger(OneLimiterQpsCleaner.class);
    private static final int ROLLING_STATE_TIME = 90;
    private static final int CLEAN_INTERVAL_SECOND = 5;
    private static ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private static AtomicBoolean started = new AtomicBoolean(false);

    public static void init() {
        if (started.compareAndSet(false, true)) {
            logger.info("Rhino oneLimiter qps data cleaner task start!!!");
            long initialDelay = 1000 - (System.currentTimeMillis() % 1000);
            scheduledExecutor.scheduleAtFixedRate(new CleanerTask(), initialDelay, CLEAN_INTERVAL_SECOND * 1000L, TimeUnit.MILLISECONDS);
        }
    }

    private static class CleanerTask implements Runnable {

        /**
         * 记录task执行次数情况（0～11），每当 count==0 时，尝试上报告警信息。
         */
        private int count = 0;

        @Override
        public void run() {
            //此线程5秒执行一次，告警上报60秒，每执行12次就会尝试上报限流告警
            count = (count + 1) % (OneLimiterAlarm.getAlarmIntervalSeconds() / CLEAN_INTERVAL_SECOND);
            boolean needReport = OneLimiterAlarm.isAlarmSwitchOpen() && count == 0;
            Map<String, OneLimiterQpsMetric> oneLimiterQpsMetrics = OneLimiterQpsMetric.getOneLimiterQpsMetrics();
            long currentTimeMillis = System.currentTimeMillis();
            int index = (int) (currentTimeMillis / 1000) % 100;
            for (String key : oneLimiterQpsMetrics.keySet()) {
                try {
                    OneLimiterQpsMetric oneLimiterQpsMetric = oneLimiterQpsMetrics.get(key);
                    for (int i = 1; i <= CLEAN_INTERVAL_SECOND; i++) {
                        int preIndex = index - ROLLING_STATE_TIME - i;
                        preIndex = preIndex >= 0 ? preIndex : preIndex + 100;
                        OneLimiterQpsBucket oneLimiterQpsBucket = oneLimiterQpsMetric.getOneLimiterQpsBucket(preIndex);
                        oneLimiterQpsBucket.clear();
                    }
                    if (needReport) {
                        oneLimiterQpsMetric.report(currentTimeMillis, index);
                    }
                } catch (Exception e) {
                    logger.warn("one limiter qps metric clear failed, metric key => " + key, e);
                }
            }
        }
    }
}
