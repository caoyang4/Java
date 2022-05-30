package src.rhino.onelimiter.matcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhanjun on 2018/6/28.
 */
public class StringInSetMatcher extends AbstractParamMatcher {

    private Set<String> valueSet;

    public StringInSetMatcher(String pattern) {
        this.valueSet = new HashSet<>(Arrays.asList(pattern.split(",")));
    }

    @Override
    public boolean match(String value) {
        return valueSet.contains(value);
    }
}
