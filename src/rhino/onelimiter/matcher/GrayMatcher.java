package src.rhino.onelimiter.matcher;

import java.util.Arrays;
import java.util.HashSet;

import com.mysql.cj.util.StringUtils;

import src.rhino.util.AppUtils;

/**
 * Created by zhen on 2019/1/11.
 */
public class GrayMatcher extends AbstractParamMatcher {

    private boolean match;

    public GrayMatcher(String pattern) {
        match = !StringUtils.isNullOrEmpty(pattern)
                && new HashSet<>(Arrays.asList(pattern.split(","))).contains(AppUtils.getLocalIp());
    }

    @Override
    public boolean match(String value) {
        return match;
    }
}
