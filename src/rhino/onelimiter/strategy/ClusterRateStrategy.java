package src.rhino.onelimiter.strategy;

import java.util.Map;

import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.util.AppUtils;

/**
 * 集群精确限流策略
 * Created by zhanjun on 2018/4/14.
 */
public class ClusterRateStrategy extends AbstractClusterStrategy {

    public ClusterRateStrategy(OneLimiterStrategy strategy) {
        super(strategy);
    }

    /**
     * @param strategy
     * @return
     */
    @Override
    protected String genUniqueKey(OneLimiterStrategy strategy) {
        int expireSeconds = (int) strategy.getTimeUnit().toSeconds(strategy.getDuration());

        StringBuilder redisKey = new StringBuilder();
        if(strategy.isSetMode()){
            //如果开启了SET隔离，就在redisKey中带上set标识
            redisKey.append(AppUtils.getSet()).append(".");
        }

        redisKey.append(AppUtils.getAppName())
                .append(".").append(strategy.getRhinoKey())
                .append(".").append(strategy.getStrategyEnum().getName())
                .append(".").append(strategy.getEntrance())
                .append(".").append(expireSeconds);

        if (strategy.getFormattedParams() != null) {
            redisKey.append(".").append(strategy.getFormattedParams());
        }
        return redisKey.toString();
    }

    @Override
    protected ClusterLimiter buildClusterLimiter(OneLimiterStrategy strategy) {
        return clusterLimiterBuilder.build(strategy);
    }
    
    protected String wrapStoreKey(String key, Map<String, String> reqParams){
        return AppUtils.getAppName() + "." + key;
    }

    @Override
    boolean isParamIllegal(Map<String, String> reqParams) {
        return false;
    }
    
}
