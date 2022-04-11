package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 132. 分割回文串 II
 * 输入：s = "aab"
 * 输出：1
 * @author caoyang
 */
public class Leetcode132 {
    public static int minCut(String s) {
        char[] chars = s.toCharArray();
        int size = chars.length;
        boolean[][] isPalindrome = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if(i <= j){ isPalindrome[i][j] = isPalindromeString(chars, i, j); }
            }
        }
        int[] dp = new int[size+1];
        for (int i = 1; i <= size; i++) {
            dp[i] = i;
            for (int j = 0; j < size; j++) {
                if(isPalindrome[j][i-1]){
                    dp[i] = Math.min(dp[j]+1, dp[i]);
                }
            }
        }
        return dp[size] - 1;
    }

    public static boolean isPalindromeString(char[] chars,int start, int end){
        while (start < end){
            if(chars[start++] != chars[end--]){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String s = "coder";
        int result = minCut(s);
        System.out.println(result);
    }
}
