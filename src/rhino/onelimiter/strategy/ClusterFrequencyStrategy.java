package src.rhino.onelimiter.strategy;

import java.util.Map;

import com.mysql.cj.util.StringUtils;
import org.springframework.util.CollectionUtils;

import src.rhino.onelimiter.OneLimiterConstants;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.util.AppUtils;

/**
 * 集群限频策略
 * Created by zhanjun on 2018/4/14.
 */
public class ClusterFrequencyStrategy extends AbstractClusterStrategy {

    public ClusterFrequencyStrategy(OneLimiterStrategy strategy) {
        super(strategy);
    }

    @Override
    String genUniqueKey(OneLimiterStrategy strategy) {
        int expireSeconds = (int) strategy.getTimeUnit().toSeconds(strategy.getDuration());

        StringBuilder tokenName = new StringBuilder();
        if(strategy.isSetMode()){
            //如果开启了SET隔离，就在redisKey中带上set标识
            tokenName.append(AppUtils.getSet()).append(".");
        }

        tokenName.append(AppUtils.getAppName())
                .append(".").append(strategy.getRhinoKey())
                .append(".").append(strategy.getStrategyEnum().getName())
                .append(".").append(strategy.getEntrance())
                .append(".").append(expireSeconds);
        if (strategy.getFormattedParams() != null) {
            tokenName.append(".").append(strategy.getFormattedParams());
        }
        return tokenName.toString();
    }

    @Override
    ClusterLimiter buildClusterLimiter(OneLimiterStrategy strategy) {
        return clusterLimiterBuilder.buildFrequency(strategy);
    }
    
    @Override
    boolean isParamIllegal(Map<String, String> reqParams) {
    	if (CollectionUtils.isEmpty(reqParams)) {
            return true;
        }
    	String userKey = reqParams.get(OneLimiterConstants.UUID);
        if (StringUtils.isNullOrEmpty(userKey)) {
            userKey = reqParams.get(OneLimiterConstants.IP);
            if (StringUtils.isNullOrEmpty(userKey)) {
                return true;
            }
        }
        return false;
    }
    
    String wrapStoreKey(String key, Map<String, String> reqParams){
        String userKey = reqParams.get(OneLimiterConstants.UUID);
        if (StringUtils.isNullOrEmpty(userKey)) {
            userKey = reqParams.get(OneLimiterConstants.IP);
        }
        return AppUtils.getAppName() + "." + tokenName + "." + userKey;
    }
    
   
}
