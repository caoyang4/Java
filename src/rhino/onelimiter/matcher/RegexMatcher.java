package src.rhino.onelimiter.matcher;

import java.util.regex.Pattern;

/**
 * Created by zhanjun on 2018/6/28.
 */
public class RegexMatcher extends AbstractParamMatcher {

    private Pattern pattern;

    public RegexMatcher(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean match(String value) {
        return pattern.matcher(value).matches();
    }
}
