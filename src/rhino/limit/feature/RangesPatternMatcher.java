package src.rhino.limit.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.PatternSyntaxException;

/**
 * Created by zhanjun on 2017/8/13.
 */
public class RangesPatternMatcher extends AbstractPatternMatcher {

    private static final ConcurrentHashMap<String, RangesPattern> holder = new ConcurrentHashMap<>();
    public static final PatternMatcher INSTANCE = new RangesPatternMatcher();

    @Override
    public boolean doMatch(String pattern, String text, FeaturePatternEnum patternEnum) {
        RangesPattern rangesPattern = holder.get(pattern);
        if (rangesPattern == null) {
            try {
                rangesPattern = new RangesPattern().parse(pattern);
                holder.putIfAbsent(pattern, rangesPattern);
            } catch (Exception e) {
                return false;
            }
        }
        return rangesPattern.match(text);
    }

    private static class RangesPattern {

        private List<Range> rangeList = new ArrayList<>();

        /**
         *
         * @param pattern
         * @return
         */
        public RangesPattern parse(String pattern) {
            String[] rangePatterns = pattern.split("\\|\\|");
            for (String rangePattern : rangePatterns) {
                Range range = new Range(rangePattern);
                rangeList.add(range);
            }
            return this;
        }

        /**
         *
         * @param text
         * @return
         */
        public boolean match(String text) {
            Double textAsNumber;
            try {
                textAsNumber = Double.parseDouble(text);
                for (Range range : rangeList) {
                    if (range.match(textAsNumber)) {
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                //ignore exception
            }
            return false;
        }
    }

    private static class Range {

        private double lower;
        private double upper;
        private boolean includeLower;
        private boolean includeUpper;
        private char[] chars;
        private int cursor;

        public Range(String pattern) {
            this.chars = pattern.toCharArray();
            doParse();
            this.chars = null;
        }

        /**
         *
         * @param value
         * @return
         */
        public boolean match(double value) {
            return (includeLower ? Double.compare(lower, value) <= 0 : Double.compare(lower, value) < 0) &&
                    (includeUpper ? Double.compare(upper, value) >= 0 : Double.compare(upper, value) > 0);
        }

        private void doParse() {
            this.includeLower = parseLeftBoundary();
            this.lower = parseLeftValue();
            skipComma();
            this.upper = parseRightValue();
            this.includeUpper = parseRightBoundary();
        }

        private double parseLeftValue() {
            skipWhitespace();
            if (read() == '*') {
                return Double.NEGATIVE_INFINITY;
            }
            cursor--;
            String value = getLeftValueString();
            return Double.parseDouble(value);
        }

        private double parseRightValue() {
            skipWhitespace();
            if (read() == '*') {
                return Double.POSITIVE_INFINITY;
            }
            cursor--;
            String value = getRightValueString();
            return Double.parseDouble(value);
        }

        private String getLeftValueString() {
            char[] temp = new char[128];
            int i = 0;
            while (true) {
                char c = read();
                if (c == ',') {
                    cursor--;
                    break;
                }
                temp[i++] = c;
            }
            return new String(temp);
        }

        private String getRightValueString() {
            char[] temp = new char[128];
            int i = 0;
            while (true) {
                char c = read();
                if (c == ']') {
                    cursor--;
                    break;
                }
                temp[i++] = c;
            }
            return new String(temp);
        }

        private void skipWhitespace() {
            while (Character.isWhitespace(chars[cursor])) {
                cursor++;
            }
        }

        private void skipComma() {
            skipWhitespace();
            char c = read();
            if (c != ',') {
                throw new PatternSyntaxException("", new String(chars), cursor - 1);
            }
        }

        private char read() {
            return chars[cursor++];
        }

        private boolean parseLeftBoundary() {
            skipWhitespace();
            char c = read();
            switch (c) {
                case '[':
                    return true;
                case '(':
                    return false;
                default:
                    throw new PatternSyntaxException("", new String(chars), cursor - 1);
            }
        }

        private boolean parseRightBoundary() {
            skipWhitespace();
            char c = read();
            switch (c) {
                case ']':
                    return true;
                case ')':
                    return false;
                default:
                    throw new PatternSyntaxException("", new String(chars), cursor - 1);
            }
        }
    }
}
