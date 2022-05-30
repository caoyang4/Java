package src.rhino.limit.feature;

/**
 * Created by zhanjun on 2017/8/13.
 */
public interface PatternMatcher {

    boolean match(String pattern, String text, FeaturePatternEnum patternEnum);

}
