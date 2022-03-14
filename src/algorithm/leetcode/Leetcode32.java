package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 32. 最长有效括号
 *
 * 给你一个只包含 '(' 和 ')' 的字符串，找出最长有效（格式正确且连续）括号子串的长度。
 * 输入：s = "(()"
 * 输出：2
 * @author caoyang
 */
public class Leetcode32 {
    /**
     * 区间型动态规划
     * @param s
     * @return
     */
    public static int longestValidParentheses(String s) {
        if(s == null || s.length() < 2) { return 0; }
        Deque<Integer> stack = new LinkedList<>();
        // 长度比索引多1
        stack.push(-1);
        char[] chars = s.toCharArray();
        int maxLen = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '('){
                stack.push(i);
            } else {
                stack.pop();
                // 当出现右括号多于左括号时，将右括号的索引压栈
                if (stack.isEmpty()){
                    stack.push(i);
                }
                maxLen = Math.max(maxLen, i - stack.peek());
            }
        }
        return maxLen;
    }

    public static void main(String[] args) {
        String s = "()(()))()";
        int res = longestValidParentheses(s);
        System.out.println(res);

    }
}
