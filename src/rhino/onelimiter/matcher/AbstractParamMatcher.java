package src.rhino.onelimiter.matcher;

import java.util.Set;

/**
 * 提供默认方法，不同的matcher需要重载不同的方法
 * Created by zhanjun on 2018/6/28.
 */
public abstract class AbstractParamMatcher implements ParamMatcher {

    @Override
    public boolean match(String value) {
        return false;
    }

    @Override
    public boolean match(Set<String> vlaues) {
        return false;
    }
}
