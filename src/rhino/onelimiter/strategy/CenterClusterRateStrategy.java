package src.rhino.onelimiter.strategy;

import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.util.AppUtils;

/**
 * 集中式限流策略（没有SET隔离）
 * Created by zhanjun on 2018/6/8.
 */
public class CenterClusterRateStrategy extends ClusterRateStrategy {

    public CenterClusterRateStrategy(OneLimiterStrategy strategy) {
        super(strategy);
    }

    @Override
    public String genUniqueKey(OneLimiterStrategy strategy) {
        StringBuilder redisKey = new StringBuilder(AppUtils.getAppName())
                .append(".").append(strategy.getRhinoKey())
                .append(".").append(strategy.getStrategyEnum())
                .append(".").append(strategy.getEntrance());
        if (strategy.getFormattedParams() != null) {
            redisKey.append(".").append(strategy.getFormattedParams());
        }
        return redisKey.toString();
    }

    @Override
    public ClusterLimiter buildClusterLimiter(OneLimiterStrategy strategy) {
        return clusterLimiterBuilder.buildCenter(strategy);
    }
}
