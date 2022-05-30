package src.rhino.onelimiter.matcher;

import java.util.Set;

/**
 * Created by zhanjun on 2018/6/28.
 */
public interface ParamMatcher {

    /**
     * 字符创匹配
     * @param value
     * @return
     */
    boolean match(String value);

    /**
     * 字符串集合匹配
     * @param vlaues
     * @return
     */
    boolean match(Set<String> values);
}
