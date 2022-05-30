package src.rhino.limit.feature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanjun on 2017/8/13.
 */
public class SetPatternMatcher extends AbstractPatternMatcher {

    private ConcurrentHashMap<String, Set<String>> holder = new ConcurrentHashMap<>();
    public static final PatternMatcher INSTANCE = new SetPatternMatcher();

    @Override
    public boolean doMatch(String pattern, String text, FeaturePatternEnum patternEnum) {
        if (pattern == null || text == null) {
            return false;
        }
        Set<String> patterns = holder.get(pattern);
        if (patterns == null) {
            patterns = new HashSet<>(Arrays.asList(pattern.split(",")));
            Set<String> patterns0 = holder.putIfAbsent(pattern, patterns);
            if (patterns0 != null) {
                patterns = patterns0;
            }
        }

        switch (patternEnum) {
            case STRING_IN_SET: return patterns.contains(text);
            case STRING_NOT_IN_SET: return !patterns.contains(text);
        }
        return false;
    }
}
