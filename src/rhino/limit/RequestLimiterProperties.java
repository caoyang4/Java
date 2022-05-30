package src.rhino.limit;

import java.util.List;

import src.rhino.RhinoProperties;
import src.rhino.config.Configuration;
import src.rhino.limit.feature.LimiterRule;

/**
 * @author zhanjun on 2017/4/28.
 */
public interface RequestLimiterProperties extends RhinoProperties {
    /**
     * return component is active or not
     *
     * @return
     */
    boolean default_isActive = false;

    String IS_ACTIVE_FIELD = "isActive";

    String configKeySuffix = "props";

    /**
     *
     * @return
     */
    boolean getIsActive();

    /**
     *
     * @param isActive
     * @return
     */
    RequestLimiterProperties setActive(boolean isActive);

    /**
     * return the speed to limit
     *
     * @return
     */
    int default_rate = 100;

    /**
     *
     * @return
     */
    int getRate();

    /**
     *
     * @param rate
     * @return
     */
    RequestLimiterProperties setRate(int rate);

    /**
     * return handle strategy of the beyond request
     *
     * @return
     */
    int default_limiterStrategy = LimiterHandlerEnum.EXCEPTION.getType();

    /**
     *
     * @return
     */
    int getStrategy();

    /**
     *
     * @param strategy
     * @return
     */
    RequestLimiterProperties setStrategy(int strategy);

    /**
     * if the handle strategy is wait, return the time can wait, default 0
     *
     * @return
     */
    int default_timeoutInMilliseconds = 0;

    long getTimeoutInMilliseconds();

    RequestLimiterProperties setTimeoutInMilliseconds(long timeoutInMilliseconds);

    /**
     * set / get limiter rules
     * @return
     */
    String LIMITER_RULES_FIELD = "limiterRules";

    RequestLimiterProperties setLimiterRules(List<LimiterRule> limiterRules);

    List<LimiterRule> getLimiterRules();

    class Factory {

        /**
         * create DefaultRequestLimiterProperties with specified rhinoKey
         *
         * @param rhinoKey
         * @return
         */
        public static RequestLimiterProperties create(String rhinoKey) {
            return new DefaultRequestLimiterProperties(rhinoKey);
        }

        /**
         * create DefaultRequestLimiterProperties with specified appKey, rhinoKey, configuration
         *
         * @param appKey
         * @param rhinoKey
         * @param config
         * @return
         */
        public static RequestLimiterProperties create(String appKey, String rhinoKey, Configuration config) {
            return new DefaultRequestLimiterProperties(appKey, rhinoKey, config);
        }
    }
}
