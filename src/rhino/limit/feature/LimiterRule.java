package src.rhino.limit.feature;

import java.io.IOException;
import java.util.List;

import src.rhino.util.SerializerUtils;

/**
 * Created by zhanjun on 2017/8/13.
 */
public final class LimiterRule {

    /**
     * unique rule id
     */
    private String id;

    /**
     * rule name
     */
    private String name;

    /**
     * rule is active or not
     */
    private boolean active;

    /**
     * rule priority
     */
    private int priority;

    /**
     * limit strategy
     * @see src.rhino.limit.strategy.LimiterStrategyEnum
     */
    private int limiterStrategy;

    /**
     * limit strategy parameters
     */
    private LimiterStrategyParams limiterStrategyParams;

    /**
     * match mode
     * @see MatchModeEnum
     */
    private int matchMode;

    /**
     * feature pattern
     */
    private List<FeaturePattern> featurePatterns;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getLimiterStrategy() {
        return limiterStrategy;
    }

    public void setLimiterStrategy(int limiterStrategy) {
        this.limiterStrategy = limiterStrategy;
    }

    public LimiterStrategyParams getLimiterStrategyParams() {
        return limiterStrategyParams;
    }

    public void setLimiterStrategyParams(LimiterStrategyParams limiterStrategyParams) {
        this.limiterStrategyParams = limiterStrategyParams;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(int matchMode) {
        this.matchMode = matchMode;
    }

    public List<FeaturePattern> getFeaturePatterns() {
        return featurePatterns;
    }

    public void setFeaturePatterns(List<FeaturePattern> featurePatterns) {
        this.featurePatterns = featurePatterns;
    }

    @Override
    public String toString() {
        try {
            return SerializerUtils.write(this);
        } catch (IOException e) {
            return "";
        }
    }
}
