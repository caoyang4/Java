package src.rhino.onelimiter.matcher;

import java.util.Objects;

/**
 * Created by zhanjun on 2018/6/28.
 */
public class StringEqualsMatcher extends AbstractParamMatcher {

    private String pattern;

    public StringEqualsMatcher(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean match(String value) {
        return Objects.equals(value, pattern);
    }
}
