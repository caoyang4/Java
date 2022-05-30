package src.rhino.onelimiter.cluster.strategy;

import java.nio.charset.Charset;
import java.util.Map;

import com.mysql.cj.util.StringUtils;
import org.springframework.util.CollectionUtils;

import src.rhino.onelimiter.ExecuteResult;
import src.rhino.onelimiter.ExecuteStatus;
import src.rhino.onelimiter.OneLimiterConstants;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.onelimiter.cluster.data.FrequencyRequest;
import src.rhino.util.AppUtils;
import src.rhino.util.MtraceUtils;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * @author Feng
 * @date 2020-06-17
 */

public class NettyFrequencyStrategy extends AbstractClusterLimiterStrategy {
	protected static final HashFunction hashFunc = Hashing.murmur3_32();
	protected String tokenName;
	
	public NettyFrequencyStrategy(OneLimiterStrategy strategy) {
		super(strategy);
		String uniqueKey = genUniqueKey(strategy);
        Hasher hasher = hashFunc.newHasher().putString(uniqueKey, Charset.defaultCharset());
        this.tokenName = hasher.hash().toString();
	}
	

    String genUniqueKey(OneLimiterStrategy strategy) {
        int expireSeconds = (int) strategy.getTimeUnit().toSeconds(strategy.getDuration());
        StringBuilder tokenName = new StringBuilder(AppUtils.getSet())
                .append(".").append(AppUtils.getAppName())
                .append(".").append(strategy.getRhinoKey())
                .append(".").append(strategy.getStrategyEnum().getName())
                .append(".").append(strategy.getEntrance())
                .append(".").append(expireSeconds);
        if (strategy.getFormattedParams() != null) {
            tokenName.append(".").append(strategy.getFormattedParams());
        }
        return tokenName.toString();
    }

    /**
     * 集群限频策略
     *
     * @param entrance 无效参数
     * @param reqParams 基于uuid进行隔离
     * @param tokens 请求所需令牌数
     * @return
     */
	@Override
	public ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens) {
		boolean isTest = MtraceUtils.isTest();
		if(strategy.getThreshold() == 0) {
			return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
		}
		
		if (CollectionUtils.isEmpty(reqParams)) {
			return ExecuteResult.CONTINUE;
		}

		String userKey = reqParams.get(OneLimiterConstants.UUID);
		if (StringUtils.isNullOrEmpty(userKey)) {
			userKey = reqParams.get(OneLimiterConstants.IP);
			if (StringUtils.isNullOrEmpty(userKey)) {
				return ExecuteResult.CONTINUE;
			}
		}

		String uniqKey = tokenName + "." + userKey;

		ExecuteStatus status = acquireFrequencyToken(new FrequencyRequest(strategy.getStrategyId(), tokens, isTest,uniqKey));
		if (status.isReject()) {
			return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
		}
		if (status.isWarn()) {
			return ExecuteResult.CONTINUE_AND_WARN;
		}
		return ExecuteResult.CONTINUE;
	}
	
	

}
