package src.rhino.limit.feature;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by zhanjun on 2017/7/3.
 */
public class AntPatternMatcher extends AbstractPatternMatcher {

    private static final ConcurrentHashMap<String, Pattern> holder = new ConcurrentHashMap<>();
    public static final PatternMatcher INSTANCE = new AntPatternMatcher();

    @Override
    public boolean doMatch(String pattern, String text, FeaturePatternEnum patternEnum) {
        return false;
    }
}
