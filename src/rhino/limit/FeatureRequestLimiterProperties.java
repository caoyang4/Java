package src.rhino.limit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mysql.cj.util.StringUtils;

import src.rhino.RhinoType;
import src.rhino.annotation.JsonIgnore;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.Configuration;
import src.rhino.limit.feature.FeaturePattern;
import src.rhino.limit.feature.LimiterRule;
import src.rhino.util.AppUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zhanjun on 2017/08/21.
 */
public class FeatureRequestLimiterProperties extends AbstractRequestLimiterProperties {

    private boolean isActive;

    @JsonIgnore
    private volatile List<LimiterRule> limiterRules;

    public FeatureRequestLimiterProperties(String rhinoKey) {
        this(AppUtils.getAppName(), rhinoKey, null);
    }

    public FeatureRequestLimiterProperties(String appKey, String rhinoKey, Configuration configuration) {
        super(appKey, rhinoKey, RhinoType.FeatureLimiter, configuration);
        boolean isActive = default_isActive;
        this.isActive = getIsActive(isActive);
        this.limiterRules = parseLimiterRule(getStringValue(LIMITER_RULES_FIELD, ""));
    }

    @Override
    public boolean getIsActive() {
        return getIsActive(isActive);
    }

    @Override
    public RequestLimiterProperties setActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    private boolean getIsActive(boolean defaultValue) {
        return getBooleanValue(IS_ACTIVE_FIELD, defaultValue);
    }

    @Override
    public List<LimiterRule> getLimiterRules() {
        return limiterRules;
    }

    @Override
    public RequestLimiterProperties setLimiterRules(List<LimiterRule> limiterRules) {
        this.limiterRules = limiterRules;
        return this;
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        addPropertiesChangedListener(LIMITER_RULES_FIELD, new ConfigChangedListener() {
            @Override
            public void invoke(String key, String oldValue, String newValue) {
                if (newValue == null || oldValue == null || newValue.equals(oldValue)) {
                    return;
                }

                // TODO 如果规则删完了，怎么办
                List<LimiterRule> limiterRules = parseLimiterRule(newValue);
                if (limiterRules != null) {
                    FeatureRequestLimiterProperties.this.setLimiterRules(limiterRules);
                }
            }
        });
    }

    /**
     * @param value
     * @return
     */
    private List<LimiterRule> parseLimiterRule(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            return new ArrayList<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<LimiterRule> limiterRules = mapper.readValue(value, new TypeReference<List<LimiterRule>>() {
            });
            List<LimiterRule> activeRules = new ArrayList<>();
            for (LimiterRule limiterRule : limiterRules) {
                List<FeaturePattern> featurePatterns = limiterRule.getFeaturePatterns();
                if (limiterRule.isActive() && (featurePatterns != null && !featurePatterns.isEmpty())) {
                    activeRules.add(limiterRule);
                }
            }

            Collections.sort(activeRules, new Comparator<LimiterRule>() {

                @Override
                public int compare(LimiterRule o1, LimiterRule o2) {
                    return o1.getPriority() < o2.getPriority() ? 1 : -1;
                }
            });
            return activeRules;
        } catch (Exception e) {
            logger.error("limiter rule parse error", e);
        }
        return new ArrayList<>();
    }

    @Override
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append("\"");
            builder.append("isActive");
            builder.append("\"");
            builder.append(":");
            builder.append(isActive);
            if (limiterRules != null && !limiterRules.isEmpty()) {
                builder.append(",");
                builder.append("\"");
                builder.append(LIMITER_RULES_FIELD);
                builder.append("\"");
                builder.append(":");
                builder.append(mapper.writeValueAsString(limiterRules));
            }
            builder.append("}");
            return builder.toString();
        } catch (JsonProcessingException e) {
            logger.warn("FeatureRequestLimiterProperties toJson error"+e.getMessage());
        }
        return "";

    }
}
