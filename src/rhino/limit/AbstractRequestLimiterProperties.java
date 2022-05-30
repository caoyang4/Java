package src.rhino.limit;

import java.util.ArrayList;
import java.util.List;

import src.rhino.RhinoConfigProperties;
import src.rhino.RhinoType;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.Configuration;
import src.rhino.limit.feature.LimiterRule;

/**
 * Created by zhanjun on 2017/08/21.
 */
public class AbstractRequestLimiterProperties extends RhinoConfigProperties implements RequestLimiterProperties {

    public AbstractRequestLimiterProperties(String appKey, String rhinoKey, RhinoType rhinoType, Configuration config) {
        super(appKey, rhinoKey, rhinoType, config);
    }

    @Override
    public boolean getIsActive() {
        return false;
    }

    @Override
    public RequestLimiterProperties setActive(boolean isActive) {
        return null;
    }

    @Override
    public int getRate() {
        return 0;
    }

    @Override
    public RequestLimiterProperties setRate(int rate) {
        return null;
    }

    @Override
    public int getStrategy() {
        return 0;
    }

    @Override
    public RequestLimiterProperties setStrategy(int strategy) {
        return this;
    }

    @Override
    public long getTimeoutInMilliseconds() {
        return 0;
    }

    @Override
    public RequestLimiterProperties setTimeoutInMilliseconds(long timeoutInMilliseconds) {
        return this;
    }

    @Override
    public RequestLimiterProperties setLimiterRules(List<LimiterRule> limiterRules) {
        return this;
    }

    @Override
    public List<LimiterRule> getLimiterRules() {
        return new ArrayList<>();
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        //Do nothing
    }
}
