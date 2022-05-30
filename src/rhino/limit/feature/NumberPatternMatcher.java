package src.rhino.limit.feature;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanjun on 2017/8/13.
 */
public class NumberPatternMatcher extends AbstractPatternMatcher {

    private static final ConcurrentHashMap<String, Double> holder = new ConcurrentHashMap<>();
    public static final PatternMatcher INSTANCE = new NumberPatternMatcher();

    @Override
    public boolean doMatch(String pattern, String text, FeaturePatternEnum patternEnum) {
        Double cacheNumber = holder.get(pattern);
        if (cacheNumber == null) {
            try {
                cacheNumber = Double.parseDouble(pattern);
                holder.putIfAbsent(pattern, cacheNumber);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        try {
            Double textDouble = Double.parseDouble(text);
            int result = textDouble.compareTo(cacheNumber);
            switch (patternEnum) {
                case NUMBER_GREATER_OR_EQUAL_THAN: return result >= 0;
                case NUMBER_GREATER_THAN: return result > 0;
                case NUMBER_LESS_OR_EQUAL_THAN: return result <= 0;
                case NUMBER_LESS_THAN: return result < 0;
            }
        } catch (NumberFormatException e) {
            //ignore exception
        }
        return false;
    }
}
