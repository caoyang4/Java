package src.rhino.onelimiter.alarm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.service.RhinoManager;
import src.rhino.util.AppUtils;

/**
 * Created by zhen on 2018/11/28.
 */
public class OneLimiterQpsMetric {

    private static ConcurrentHashMap<String, OneLimiterQpsMetric> oneLimiterQpsMetrics = new ConcurrentHashMap<>();
    private static String appKey = AppUtils.getAppName();
    private static final int BUCKET_NUM = 100;
    private static int SUMMARY_DURATION = 90;

    private String key;
    private OneLimiterStrategy strategy;
    private OneLimiterQpsBucket[] oneLimiterQpsBuckets;

    public OneLimiterQpsMetric(OneLimiterStrategy strategy) {
        this.strategy = strategy;
        this.key = strategy.getRhinoKey() + "-" + strategy.getCatEventTag();
        //区分同名的压测策略与正常策略的metrix
        if (strategy.isTest()) {
            key += "-test";
        }
        this.oneLimiterQpsBuckets = new OneLimiterQpsBucket[BUCKET_NUM];
        for (int i = 0; i < BUCKET_NUM; i++) {
            oneLimiterQpsBuckets[i] = new OneLimiterQpsBucket();
        }
        oneLimiterQpsMetrics.put(key, this);
    }

    public long markSuccess(int tokenNum) {
        OneLimiterQpsBucket target = getCurrentBucket();
        return target.markSuccess(tokenNum);
    }

    public void markReject(int tokenNum) {
        OneLimiterQpsBucket target = getCurrentBucket();
        target.markReject(tokenNum);
    }

    public void markWarn(int tokenNum) {
        OneLimiterQpsBucket target = getCurrentBucket();
        target.markWarn(tokenNum);
    }

    private OneLimiterQpsBucket getCurrentBucket() {
        int index = (int) ((System.currentTimeMillis() / 1000) % 100);
        return oneLimiterQpsBuckets[index];
    }

    public OneLimiterQpsBucket getOneLimiterQpsBucket(int index) {
        return oneLimiterQpsBuckets[index];
    }

    /**
     * @param currentTimeMillis
     * @param index
     */
    public void report(long currentTimeMillis, int index) {
        //统计信息，rejectCount>0 || warnCount>0才会上报
        long rejectCount = 0;
        long warnCount = 0;

        List<OneLimiterQpsEntity> qpsEntities = new ArrayList<>(SUMMARY_DURATION);
        //结束秒为当前秒的前一秒，保证统计的每一秒都是完整的数据（即秒完整走过的数据）
        int alarmIntervalSeconds = OneLimiterAlarm.getAlarmIntervalSeconds();
        for (int i = 1; i <= SUMMARY_DURATION; i++) {
            //选中bucket的时间戳
            long time = currentTimeMillis - 1000 * i;
            int j = index - i;
            if (j < 0) {
                j = BUCKET_NUM + j;
            }
            OneLimiterQpsBucket target = oneLimiterQpsBuckets[j];
            //count为当前一分钟的数据，因此只统计六十秒的数据；
            if (i <= alarmIntervalSeconds) {
                rejectCount += target.rejectCount.get();
                warnCount += target.warnCount.get();
            }
            qpsEntities.add(target.asEntity(time));
        }

        doReport(rejectCount, warnCount, qpsEntities);
    }

    /**
     * @param rejectCount
     * @param warnCount
     * @param isTest
     * @param qpsEntities
     */
    private void doReport(long rejectCount, long warnCount, List<OneLimiterQpsEntity> qpsEntities) {
        if (rejectCount > 0 || warnCount > 0) {
            OneLimiterAlarmEntity entity = new OneLimiterAlarmEntity(appKey, strategy.getRhinoKey());
            entity.setEntrance(strategy.getEntrance());
            entity.setStrategy(strategy.getStrategyEnum().getName());
            entity.setParams(strategy.getFormattedParams());
            entity.setCount(rejectCount > 0 ? rejectCount : warnCount);
            String type = rejectCount > 0 ? "reject" : "warn";
            if (strategy.isTest()) {
                type = "test_" + type;
            }
            entity.setType(type);
            entity.setQpsEntityList(qpsEntities);
            RhinoManager.reportOneLimiterAlarm(entity);
        }
    }

    /**
     * @param current
     * @return
     */
    public List<OneLimiterQpsEntity> getQpsSummary(long current) {
        List<OneLimiterQpsEntity> qpsEntities = new ArrayList<>();
        int index = (int) ((current / 1000) % 100);
        for (int i = 0; i < SUMMARY_DURATION; i++) {
            //当前bucket的时间戳
            long time = current - 1000 * i;
            int j = index - i;
            if (j < 0) {
                j = BUCKET_NUM + j;
            }
            OneLimiterQpsBucket target = oneLimiterQpsBuckets[j];
            qpsEntities.add(target.asEntity(time));
        }
        return qpsEntities;
    }

    public static ConcurrentHashMap<String, OneLimiterQpsMetric> getOneLimiterQpsMetrics() {
        return oneLimiterQpsMetrics;
    }

    @Override
    public String toString() {
        return "OneLimiterQpsMetric{" +
                "key='" + key + '\'' +
                ", oneLimiterQpsBuckets=" + Arrays.toString(oneLimiterQpsBuckets) +
                '}';
    }
}
