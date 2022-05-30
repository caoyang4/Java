package src.rhino.onelimiter.strategy;

import src.rhino.onelimiter.OneLimiterStrategy;

/**
 * Created by zhanjun on 2018/4/21.
 */
public interface ClusterLimiterBuilder {

    /**
     * @param strategy
     * @return
     */
    ClusterLimiter build(OneLimiterStrategy strategy);

    /**
     *
     * @param strategy
     * @return
     */
    ClusterLimiter buildCenter(OneLimiterStrategy strategy);

    /**
     *
     * @param strategy
     * @return
     */
    ClusterLimiter buildFrequency(OneLimiterStrategy strategy);
}
