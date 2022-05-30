package src.rhino.limit.feature;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by zhanjun on 2017/8/13.
 */
public class RegexPatternMatcher extends AbstractPatternMatcher {

    private static final ConcurrentHashMap<String, Pattern> holder = new ConcurrentHashMap<>();
    public static final PatternMatcher INSTANCE = new RegexPatternMatcher();

    @Override
    public boolean doMatch(String pattern, String text, FeaturePatternEnum patternEnum) {
        Pattern compiledPattern = holder.get(pattern);
        if (compiledPattern == null) {
            compiledPattern = Pattern.compile(pattern);
            Pattern compiledPattern0 = holder.putIfAbsent(pattern, compiledPattern);
            if (compiledPattern0 != null) {
                compiledPattern = compiledPattern0;
            }
        }
        return compiledPattern.matcher(text).matches();
    }
}
