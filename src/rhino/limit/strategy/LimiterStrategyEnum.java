package src.rhino.limit.strategy;

import src.rhino.cache.RedisProperties;

/**
 * @author zhanjun on 2017/8/13.
 */
public enum LimiterStrategyEnum {

    /**
     * 限制单台机器，一段时间内的访问次数
     */
    LIMIT_ACCESS_FREQUENCY_CLIENT(1, LimiterFrequencyClientStrategy.INSTANCE),

    /**
     * 限制一个集群，一段时间内的访问次数
     */
    LIMIT_ACCESS_FREQUENCY_CLUSTER(2, null) {

        @Override
        public LimiterStrategy getLimiterStrategy(RedisProperties redisProperties) {
            return new LimiterFrequencyClusterStrategy(redisProperties);
        }
    },

    /**
     * 限制一段时间内的可访问时间
     */
    LIMIT_ACCESS_TIME(3, null),

    /**
     * 总是拒绝访问
     */
    ALWAYS_REFUSE_ACCESS(4, AlwaysRefuseStrategy.INSTANCE),

    /**
     * 总是允许访问
     */
    ALWAYS_ALLOW_ACCESS(5, AlwaysAllowStrategy.INSTANCE);

    private int value;

    private LimiterStrategy limiterStrategy;

    LimiterStrategyEnum(int value, LimiterStrategy limiterStrategy) {
        this.value = value;
        this.limiterStrategy = limiterStrategy;
    }

    public int getValue() {
        return value;
    }

    public LimiterStrategy getLimiterStrategy(RedisProperties redisProperties) {
        return limiterStrategy;
    }

    public static LimiterStrategyEnum getByValue(int value) {
        switch (value) {
            case 1: return LIMIT_ACCESS_FREQUENCY_CLIENT;
            case 2: return LIMIT_ACCESS_FREQUENCY_CLUSTER;
            case 3: return LIMIT_ACCESS_TIME;
            case 4: return ALWAYS_REFUSE_ACCESS;
            case 5: return ALWAYS_ALLOW_ACCESS;
            default: return null;
        }
    }
}
