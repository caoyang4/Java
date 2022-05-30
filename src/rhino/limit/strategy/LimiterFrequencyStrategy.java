package src.rhino.limit.strategy;

import src.rhino.limit.feature.LimiterRule;
import src.rhino.limit.feature.LimiterStrategyParams;

/**
 * @author zhanjun on 2017/8/13.
 */
public abstract class LimiterFrequencyStrategy extends AbstractLimiterStrategy {

    @Override
    public boolean doExecute(LimiterRule limiterRule, int permits) {
        LimiterStrategyParams strategyParams = limiterRule.getLimiterStrategyParams();
        if (strategyParams == null) {
           return true;
        }
        return doLimit(limiterRule.getId(), permits, strategyParams);
    }

    /**
     *
     * @param id
     * @param permits
     * @param strategyParams
     * @return
     */
    public abstract boolean doLimit(String id, int permits, LimiterStrategyParams strategyParams);
}
