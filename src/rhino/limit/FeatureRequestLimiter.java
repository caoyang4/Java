package src.rhino.limit;

import java.util.List;

import src.rhino.cache.RedisProperties;
import src.rhino.dispatcher.RhinoEvent;
import src.rhino.limit.feature.FeaturePattern;
import src.rhino.limit.feature.FeaturePatternEnum;
import src.rhino.limit.feature.Features;
import src.rhino.limit.feature.LimiterRule;
import src.rhino.limit.feature.MatchModeEnum;
import src.rhino.limit.strategy.LimiterStrategy;
import src.rhino.util.ExtensionLoader;

/**
 * @author zhanjun on 2017/7/3.
 */
public class FeatureRequestLimiter extends AbstractRequestLimiter {

    private static RedisClientFactory redisClientFactory = ExtensionLoader.newExtension(RedisClientFactory.class);
    private RequestLimiterProperties properties;
    private RedisProperties redisProperties;

    /**
     * @param key
     * @param properties
     */
    public FeatureRequestLimiter(String key, RequestLimiterProperties properties) {
        super(key);
        this.properties = properties;
    }

    @Override
    public boolean tryAcquire(Features features) {
        return tryAcquire(features, 1);
    }

    @Override
    public boolean tryAcquire(Features features, int permits) {
        if (features == null || features.isEmpty() || !properties.getIsActive()) {
            return true;
        }
        List<LimiterRule> limiterRules = properties.getLimiterRules();
        if (limiterRules == null || limiterRules.isEmpty()) {
            return true;
        }
        boolean isAccess = true;
        String preName = null;
        // 限流规则在初始化或变动时，已经按优先度从大到小排序
        for (LimiterRule limiterRule : limiterRules) {
            LimiterStrategy limiterStrategy = LimiterStrategy.Factory.getLimiterStrategy(limiterRule.getLimiterStrategy(), redisProperties);
            if (limiterStrategy == null) {
                continue;
            }
            boolean matched = isLimiterRuleMatched(limiterRule, features);
            if (matched) {
                if (preName == null) {
                    preName = features.toString();
                }
                boolean isAllowed = limiterStrategy.execute(limiterRule, permits);
                if (!isAllowed) {
                    isAccess = false;
                    break;
                }
            }
        }
        eventDispatcher.dispatchEvent(new RhinoEvent(preName, isAccess ? LimiterEventType.ACCESS : LimiterEventType.REFUSE));
        return isAccess;
    }

    /**
     *
     * @param limiterRule
     * @param features
     * @return
     */
    private boolean isLimiterRuleMatched(LimiterRule limiterRule, Features features) {
        int matchMode = limiterRule.getMatchMode();
        MatchModeEnum matchModeEnum = MatchModeEnum.getByValue(matchMode);
        if (matchModeEnum == null) {
            return false;
        }
        switch (matchModeEnum) {
            case MATCH_ALL:
                return isMatchAll(limiterRule, features);
            case MATCH_ANY:
                return isMatchAny(limiterRule, features);
            default: return false;
        }
    }

    /**
     *
     * @param limiterRule
     * @param features
     * @return
     */
    private boolean isMatchAll(LimiterRule limiterRule, Features features) {
        List<FeaturePattern> featurePatterns = limiterRule.getFeaturePatterns();
        for (FeaturePattern featurePattern : featurePatterns) {
            String featureValue = features.get(featurePattern.getName());
            if (featureValue == null) {
                return false;
            }
            int patternModeValue = featurePattern.getPatternMode();
            FeaturePatternEnum patternModeEnum = FeaturePatternEnum.getByValue(patternModeValue);
            if (patternModeEnum == null) {
                return false;
            }
            if (!patternModeEnum.match(featurePattern.getValue(), featureValue)) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param limiterRule
     * @param features
     * @return
     */
    private boolean isMatchAny(LimiterRule limiterRule, Features features) {
        List<FeaturePattern> featurePatterns = limiterRule.getFeaturePatterns();
        for (FeaturePattern featurePattern : featurePatterns) {
            String featureValue = features.get(featurePattern.getName());
            if (featureValue == null) {
                continue;
            }
            int patternMode = featurePattern.getPatternMode();
            FeaturePatternEnum patternModeEnum = FeaturePatternEnum.getByValue(patternMode);
            if (patternModeEnum == null) {
                continue;
            }
            if (patternModeEnum.match(featurePattern.getValue(), featureValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setRedisProperties(RedisProperties redisProperties) {
        if (redisClientFactory != null) {
            redisClientFactory.create(redisProperties);
        }
        this.redisProperties = redisProperties;
    }
}
