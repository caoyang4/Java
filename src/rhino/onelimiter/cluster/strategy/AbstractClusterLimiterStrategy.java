package src.rhino.onelimiter.cluster.strategy;

import src.rhino.onelimiter.ExecuteStatus;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.onelimiter.cluster.ClusterTokenService;
import src.rhino.onelimiter.cluster.TokenResult;
import src.rhino.onelimiter.cluster.TokenResultStatus;
import src.rhino.onelimiter.cluster.data.FrequencyRequest;
import src.rhino.onelimiter.cluster.data.LimiterRequest;
import src.rhino.onelimiter.strategy.LimiterStrategy;
import src.rhino.util.ExtensionLoader;
import src.rhino.util.Preconditions;

/**
 * @author Feng
 * @date 2020-06-17
 */

public abstract class AbstractClusterLimiterStrategy implements LimiterStrategy {
    protected ClusterTokenService tokenService = ExtensionLoader.newExtension(ClusterTokenService.class);
    protected OneLimiterStrategy strategy;
 
    public AbstractClusterLimiterStrategy(OneLimiterStrategy strategy) {
    	Preconditions.checkArgument(tokenService != null,"ClusterTokenClient is null, you need import jar rhino-cluster-limiter");
    	Preconditions.checkArgument(strategy.getStrategyId() != 0," strategyId not exists, regenerate your strategy,entrance is " + strategy.getEntrance());
        this.strategy = strategy;
        tokenService.setStrategy(strategy);
    }
    
    public ExecuteStatus acquireLimiterToken(LimiterRequest req) {
    	return describeResult(tokenService.requestLimiterToken(req));
    }
    
    public ExecuteStatus acquireFrequencyToken(FrequencyRequest req) {
    	return describeResult(tokenService.requestFrequencyToken(req));
    }
    
    public ExecuteStatus describeResult(TokenResult tokenResult) {
		if(tokenResult == null || tokenResult.getStatus() == null) {
			return ExecuteStatus.CONTINUE;
		}
		switch (tokenResult.getStatus().intValue()) {
			case TokenResultStatus.OK:
				return ExecuteStatus.CONTINUE;
			case TokenResultStatus.BLOCKED:
				return ExecuteStatus.DIRECT_REJECT;
			case TokenResultStatus.WARN:
				return ExecuteStatus.CONTINUE_AND_WARN;
			case TokenResultStatus.FAIL:
				return ExecuteStatus.CONTINUE;
			case TokenResultStatus.NO_RULE_EXISTS:
				return ExecuteStatus.CONTINUE;
			default:
				return ExecuteStatus.CONTINUE;
		}
			
	}
	
	

}
