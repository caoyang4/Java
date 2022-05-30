package src.rhino.onelimiter.matcher;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Created by zhanjun on 2018/6/28.
 */
public enum ParamMatchMode {

    /**
     * 正则表达式
     */
    REGEX("regex") {
        @Override
        public ParamMatcher createMatcher(String pattern) {
            return new RegexMatcher(pattern);
        }
    },

    /**
     * 字符串值相等
     */
    STRING_EQUAL("==") {
        @Override
        public ParamMatcher createMatcher(String pattern) {
            return new StringEqualsMatcher(pattern);
        }
    },

    /**
     * 字符串值不相等
     */
    STRING_NOT_EQUAL("!=") {
        @Override
        public ParamMatcher createMatcher(String pattern) {
            return new StringNotEqualsMatcher(pattern);
        }
    },

    /**
     * 字符串值在给定的集合中
     */
    STRING_IN_SET("in") {
        @Override
        public ParamMatcher createMatcher(String pattern) {
            return new StringInSetMatcher(pattern);
        }
    },

    /**
     * 字符串值不在给定的集合中
     */
    STRING_NOT_IN_SET("not in") {
        @Override
        public ParamMatcher createMatcher(String pattern) {
            return new StringNotInSetMatcher(pattern);
        }
    },

    /**
     * 压测流量判断
     */
    TEST_REQUEST("is test") {
        @Override
        public ParamMatcher createMatcher(String pattern) {
            return new TestRequestMather();
        }
    },
    /**
     * 主机灰度判断
     */
    GRAY("gray") {
        @Override
        public ParamMatcher createMatcher(String pattern) {
            return new GrayMatcher(pattern);
        }
    },

    KEY_NOT_EXIST("not exist") {
        @Override
        public ParamMatcher createMatcher(String name) {
            Set<String> keys = Sets.newHashSet(name.split(","));
            return new KeyNotExistMatcher(keys);
        }
    };



    private String symbol;

    ParamMatchMode(String symbol) {
        this.symbol = symbol;
    }

    public abstract ParamMatcher createMatcher(String pattern);

    public String getSymbol() {
        return symbol;
    }
}
