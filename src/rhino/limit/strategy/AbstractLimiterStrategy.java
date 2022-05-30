package src.rhino.limit.strategy;

import src.rhino.limit.feature.LimiterRule;
import src.rhino.log.Logger;
import src.rhino.log.LoggerFactory;

/**
 * @author zhanjun on 2017/8/13.
 */
public abstract class AbstractLimiterStrategy implements LimiterStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLimiterStrategy.class);

    @Override
    public boolean execute(LimiterRule limiterRule, int permits) {
        try {
            return doExecute(limiterRule, permits);
        } catch (Exception e) {
            logger.error("execute limiter rule:" + limiterRule.toString(), e);
            return true;
        }
    }

    /**
     * do execute limit rule
     * @param limiterRule
     * @return
     */
    public abstract boolean doExecute(LimiterRule limiterRule, int permits);
}
