package src.rhino.limit.feature;

import com.mysql.cj.util.StringUtils;

/**
 * Created by zhanjun on 2017/08/17.
 */
public abstract class AbstractPatternMatcher implements PatternMatcher {

    @Override
    public boolean match(String pattern, String text, FeaturePatternEnum patternEnum) {
        if (StringUtils.isNullOrEmpty(pattern) || StringUtils.isNullOrEmpty(text)) {
            return false;
        }
        return doMatch(pattern, text, patternEnum);
    }

    protected abstract boolean doMatch(String pattern, String text, FeaturePatternEnum patternEnum);
}
