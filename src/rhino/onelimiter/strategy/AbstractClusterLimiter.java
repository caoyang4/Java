package src.rhino.onelimiter.strategy;

import java.util.concurrent.TimeUnit;

import src.rhino.onelimiter.ExecuteStatus;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.onelimiter.alarm.OneLimiterAlarm;
import src.rhino.util.AppUtils;
import src.rhino.util.MtraceUtils;

/**
 * 集群限流器抽象类
 * Created by zhanjun on 2018/4/14.
 */
public abstract class AbstractClusterLimiter implements ClusterLimiter {

    protected static String appKey = AppUtils.getAppName();

    /**
     * 每次获取的步长
     */
    protected int step;

    /**
     * 集群限流的阈值
     */
    protected int threshold;

    /**
     * 时间单位
     */
    protected TimeUnit timeUnit;

    /**
     * 限流的周期（单位秒）
     */
    protected int expireSeconds;

    public AbstractClusterLimiter(OneLimiterStrategy strategy) {
        this.step = strategy.getStep() > 1 ? strategy.getStep() : 1;
        this.threshold = strategy.getThreshold();
        this.timeUnit = strategy.getTimeUnit();
        this.expireSeconds = (int) strategy.getTimeUnit().toSeconds(strategy.getDuration());
    }

    /**
     * 从远程中心请求令牌
     * @param appKey
     * @param key 资源标识
     * @param tokens 请求令牌数
     * @param expireSeconds 有效时间
     * @return 当前远程中心的流量记录
     */
    protected abstract int requestTokens(String appKey, String key, int tokens, int expireSeconds);

    /**
     * 默认限流算法，直接请求远程中心，然后对比当前流量和阈值
     * @param tokenName
     * @param tokens
     * @return
     */
    @Override
    public ExecuteStatus acquire(String tokenName, int tokens) {
        if (threshold <= 0) {
            return ExecuteStatus.DIRECT_REJECT;
        }

        tokenName = MtraceUtils.isTest() ? MtraceUtils.addTestFlag(tokenName) : tokenName;

        // 如果时间单位是天，则按照自然日进行过期
        if (TimeUnit.DAYS.equals(timeUnit)) {
            // 北京时间比格林威治时间快8个小时
            long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() + 8 * 60 * 60 * 1000);
            // 加上天数后缀，这样一个自然日的请求都打到同一个key
            tokenName += days;
        }

        int currentFlow = requestTokens(appKey, tokenName, tokens, expireSeconds);
        if (currentFlow == -1) {
            //远程中心请求异常
            return ExecuteStatus.CONTINUE;
        }else if (currentFlow <= threshold) {
            //当前流量没有超过阈值
            return OneLimiterAlarm.checkWarning(currentFlow, threshold) ? ExecuteStatus.CONTINUE_AND_WARN : ExecuteStatus.CONTINUE;
        }else {
            //当前流量已经超过阈值
            return ExecuteStatus.DIRECT_REJECT;
        }
    }
}
