package src.rhino.onelimiter.cluster.strategy;

import java.util.Map;

import src.rhino.onelimiter.ExecuteResult;
import src.rhino.onelimiter.ExecuteStatus;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.onelimiter.cluster.data.LimiterRequest;
import src.rhino.util.MtraceUtils;

/**
 * @author Feng
 * @date 2020-06-17
 */

public class NettyLimiterStrategy extends AbstractClusterLimiterStrategy {
	
	public NettyLimiterStrategy(OneLimiterStrategy strategy) {
		super(strategy);
	}
	

	@Override
	public ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens) {
		boolean isTest = MtraceUtils.isTest();
		if(strategy.getThreshold() == 0) {
			return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
		}
        ExecuteStatus status = acquireLimiterToken(new LimiterRequest(strategy.getStrategyId(),tokens, isTest));
        if (status.isReject()) {
            return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
        }
        if (status.isWarn()) {
            return ExecuteResult.CONTINUE_AND_WARN;
        }
        return ExecuteResult.CONTINUE;
	}

}
