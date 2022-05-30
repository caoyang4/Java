package src.rhino.onelimiter.strategy;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import src.rhino.limit.ratelimiter.GuavaRateLimiter;
import src.rhino.limit.ratelimiter.RateLimiter;
import src.rhino.onelimiter.ExecuteResult;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.util.MtraceUtils;

/**
 * 单机限流策略（基于Guava.RateLimiter）
 * Created by zhanjun on 2018/4/14.
 */
public class SingleVmStrategy implements LimiterStrategy {

    private RateLimiter rateLimiter;
    private long periodMillis;
    private int threshold;
    private OneLimiterStrategy strategy;

    public SingleVmStrategy(OneLimiterStrategy strategy) {
        this.strategy = strategy;
        TimeUnit timeUnit = strategy.getTimeUnit();
        int duration = strategy.getDuration();
        this.threshold = strategy.getThreshold();
        long periodSecond = duration;
        if (timeUnit != null) {
            periodSecond = timeUnit.toSeconds(duration);
            this.periodMillis = timeUnit.toMillis(duration);
        }
        rateLimiter = GuavaRateLimiter.create(threshold * 1.0 / periodSecond, strategy.getMaxBurstSeconds());
    }

    /**
     * 令牌桶算法单机限流
     *
     * @param entrance  无效参数
     * @param reqParams 无效参数
     * @param tokens    请求所需令牌数
     * @return
     */
    @Override
    public ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens) {
        // 如果阈值小于等于0，那直接拒绝好了
        if (threshold <= 0) {
            return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
        }

        //压测流量计数器合并正常流量计数策略,且此次请求是正常流量
        if(strategy.isTestStrategySetMerged() && !MtraceUtils.isTest()) {
            rateLimiter.acquireNoWait(tokens);
            return ExecuteResult.CONTINUE;
        }
        if(strategy.isBlock()) {
        	rateLimiter.acquire(tokens);
        	return ExecuteResult.CONTINUE;
        }else if(rateLimiter.tryAcquire(tokens, strategy.getTimeout(), TimeUnit.MILLISECONDS)) {
        	return ExecuteResult.CONTINUE;
        }

        return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
    }
}
