package src.rhino.limit.strategy;

import src.rhino.cache.RedisProperties;
import src.rhino.limit.feature.LimiterRule;

/**
 * @author zhanjun on 2017/8/13.
 */
public interface LimiterStrategy {

    /**
     * apply limiter rule
     * @param limiterRule
     * @param permits
     * @return
     */
    boolean execute(LimiterRule limiterRule, int permits);

    class Factory {

        /**
         *
         * @param value
         * @return
         */
        public static LimiterStrategy getLimiterStrategy(int value, RedisProperties redisProperties) {
            LimiterStrategyEnum limiterStrategyEnum = LimiterStrategyEnum.getByValue(value);
            if (limiterStrategyEnum != null) {
                return limiterStrategyEnum.getLimiterStrategy(redisProperties);
            }
            return null;
        }
    }
}
