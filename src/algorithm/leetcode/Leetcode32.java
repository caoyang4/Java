package src.algorithm.leetcode;

/**
 * 32. 最长有效括号
 *
 * 给你一个只包含 '(' 和 ')' 的字符串，找出最长有效（格式正确且连续）括号子串的长度。
 * 输入：s = "(()"
 * 输出：2
 * @author caoyang
 */
public class Leetcode32 {
    public static int longestValidParentheses(String s) {
        if(s == null || s.length() < 2) { return 0; }
        return 0;
    }

    public static void main(String[] args) {
        String s = "(()";
        int res = longestValidParentheses(s);
        System.out.println(res);
    }
}
