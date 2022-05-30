package src.rhino.onelimiter.strategy;

import java.nio.charset.Charset;
import java.util.Map;

import src.rhino.onelimiter.ExecuteResult;
import src.rhino.onelimiter.ExecuteStatus;
import src.rhino.onelimiter.OneLimiterStrategy;
import src.rhino.util.AppUtils;
import src.rhino.util.ExtensionLoader;
import src.rhino.util.MtraceUtils;
import src.rhino.util.Preconditions;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * 中心式集群限流策略
 * Created by zhanjun on 2018/7/2.
 */
public abstract class AbstractClusterStrategy implements LimiterStrategy {
	protected static final ClusterLimiterBuilder clusterLimiterBuilder = ExtensionLoader.newExtension(ClusterLimiterBuilder.class);
    protected static final HashFunction hashFunc = Hashing.murmur3_32();
    protected OneLimiterStrategy strategy;
    protected ClusterLimiter clusterLimiter;
    protected String tokenName;

    public AbstractClusterStrategy(OneLimiterStrategy strategy) {
        Preconditions.checkArgument(clusterLimiterBuilder != null, "clusterLimiterBuilder is null, you need import jar rhino-redis-squirrel");
        this.strategy = strategy;
        String uniqueKey = genUniqueKey(strategy);
        Hasher hasher = hashFunc.newHasher().putString(uniqueKey, Charset.defaultCharset());
        this.tokenName = hasher.hash().toString();
        this.clusterLimiter = buildClusterLimiter(strategy);
    }
    

    /**
     *
     * @param strategy
     * @return
     */
    abstract String genUniqueKey(OneLimiterStrategy strategy);

    /**
     *
     * @param strategy
     * @return
     */
    abstract ClusterLimiter buildClusterLimiter(OneLimiterStrategy strategy);

    /**
     * 组装storeKey
     * @param key
     * @param reqParams
     * @return
     */
    abstract String wrapStoreKey(String key, Map<String, String> reqParams);
    
    /**
     * 检查参数是否合法，不合法 true，合法 false
     * @param reqParams
     * @return
     */
    abstract boolean isParamIllegal(Map<String, String> reqParams);
    
    /**
     * 集群精确限流
     *
     * @param entrance  无效参数
     * @param reqParams 无效参数
     * @param tokens    请求所需令牌数
     * @return
     */
    @Override
    public ExecuteResult execute(String entrance, Map<String, String> reqParams, int tokens) {
    	if(isParamIllegal(reqParams)) {
    		return ExecuteResult.CONTINUE;
    	}
    	String reqTokenName = wrapStoreKey(tokenName,reqParams);
    	boolean isTestRequest = MtraceUtils.isTest();
    	
    	//压测流量计数器合并正常流量计数策略,且此次请求是正常流量
        if(strategy.isTestStrategySetMerged()) {
        	reqTokenName = MtraceUtils.addTestFlag(reqTokenName);
        }else {
        	reqTokenName = isTestRequest ? MtraceUtils.addTestFlag(reqTokenName) : reqTokenName;
        }
    	
        ExecuteStatus status = clusterLimiter.acquire(reqTokenName, tokens);
        //合并计数，且是正常流量，直接通过
        if(strategy.isTestStrategySetMerged() && !isTestRequest) {
        	return ExecuteResult.CONTINUE;
        }
        if (status.isReject()) {
            return ExecuteResult.createRejectResult(strategy.getCode(), strategy.getMsg());
        }
        if (status.isWarn()) {
            return ExecuteResult.CONTINUE_AND_WARN;
        }
        return ExecuteResult.CONTINUE;
    }
}
