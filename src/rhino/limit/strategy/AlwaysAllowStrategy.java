package src.rhino.limit.strategy;

import src.rhino.limit.feature.LimiterRule;

/**
 * @author zhanjun on 2017/8/13.
 */
public class AlwaysAllowStrategy extends AbstractLimiterStrategy {

    public static final LimiterStrategy INSTANCE = new AlwaysAllowStrategy();

    /**
     * always return true
     * @param limiterRule
     * @return
     */
    @Override
    public boolean doExecute(LimiterRule limiterRule, int permits) {
        return true;
    }
}
