package src.algorithm.leetcode;

/**
 * 459. 重复的子字符串
 * 输入: s = "abcabcabcabc"
 * 输出: true
 * @author caoyang
 */
public class Leetcode459 {
    public static boolean repeatedSubstringPattern(String s) {
        int len = s.length();
        if (len == 1){return false;}
        int middle = len >> 1;
        int start = 0;
        StringBuilder builder = new StringBuilder();
        while (start < middle){
            builder.append(s.charAt(start++));
            if (len % builder.length() != 0){continue;}
            if("".equals(s.replaceAll(builder.toString(), ""))){
                return true;
            }
        }
        return false;
    }
    public static boolean repeatedSubstringPattern1(String s) {
        StringBuilder builder = new StringBuilder(s);
        builder.append(s);
        // 掐头去尾
        return builder.substring(1, builder.length()-1).contains(s);
    }

    public static void main(String[] args) {
        String s = "abcabcabcabc";
        boolean result = repeatedSubstringPattern(s);
        System.out.println(result);
    }
}
