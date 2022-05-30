package src.rhino.limit.feature;

import com.alibaba.druid.util.StringUtils;
import tk.mybatis.mapper.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Created by zhanjun on 2017/8/13.
 */
public enum FeaturePatternEnum {

    /**
     * ANT模式
     */
    ANT(1, AntPatternMatcher.INSTANCE) ,

    /**
     * 正则表达式
     */
    REGEX(2, RegexPatternMatcher.INSTANCE),

    /**
     * 字符串值相等
     */
    STRING_EQUAL(3) {
        @Override
        public boolean match(String pattern, String text) {
            return Objects.equals(pattern, text);
        }
    },

    /**
     * 字符串值不相等
     */
    STRING_NOT_EQUAL(4) {
        @Override
        public boolean match(String pattern, String text) {
            return !Objects.equals(pattern, text);
        }
    },

    /**
     * 字符串值包含有
     */
    STRING_CONTAIN(5) {
        @Override
        public boolean match(String pattern, String text) {
            return text.contains(pattern);
        }
    },

    /**
     * 字符串值不包含有
     */
    STRING_NOT_CONTAIN(6) {
        @Override
        public boolean match(String pattern, String text) {
            return !text.contains(pattern);
        }
    },

    /**
     * 字符串值在给定的集合中
     */
    STRING_IN_SET(7, SetPatternMatcher.INSTANCE),

    /**
     * 字符串值不在给定的集合中
     */
    STRING_NOT_IN_SET(8, SetPatternMatcher.INSTANCE),

    /**
     * 区间表达式
     */
    NUMBER_RANGES(10, RangesPatternMatcher.INSTANCE),

    /**
     * 数字值大于
     */
    NUMBER_GREATER_THAN(11, NumberPatternMatcher.INSTANCE),

    /**
     * 数字值大于等于
     */
    NUMBER_GREATER_OR_EQUAL_THAN(12, NumberPatternMatcher.INSTANCE),

    /**
     * 数字值小于
     */
    NUMBER_LESS_THAN(13, NumberPatternMatcher.INSTANCE),
    /**
     * 数字值小于等于
     */
    NUMBER_LESS_OR_EQUAL_THAN(14, NumberPatternMatcher.INSTANCE);

    public boolean match(String pattern, String text) {
        return patternMatcher.match(pattern, text, this);
    }

    private static final Map<Integer, FeaturePatternEnum> holder = new HashMap<>();

    static {
        for (FeaturePatternEnum patternModeEnum : FeaturePatternEnum.values()) {
            holder.put(patternModeEnum.value, patternModeEnum);
        }
    }

    private int value;
    private PatternMatcher patternMatcher;

    FeaturePatternEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    FeaturePatternEnum(int value, PatternMatcher patternMatcher) {
        this.value = value;
        this.patternMatcher = patternMatcher;
    }

    public static FeaturePatternEnum getByValue(int value) {
        return holder.get(value);
    }
}
