package src.rhino.onelimiter.strategy;

import src.rhino.onelimiter.ExecuteStatus;

/**
 * 集群限流器接口
 * Created by zhanjun on 2018/4/14.
 */
public interface ClusterLimiter {

    /**
     * 请求令牌
     * @param tokenName 资源标识的唯一key
     * @param tokens 请求令牌数
     * @return
     */
    ExecuteStatus acquire(String tokenName, int tokens);

}
