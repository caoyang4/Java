package src.algorithm.leetcode;

/**
 * 44. 通配符匹配
 *
 * 给定一个字符串 (s) 和一个字符模式 (p) ，实现一个支持 '?' 和 '*' 的通配符匹配
 * '?' 可以匹配任何单个字符。
 * '*' 可以匹配任意字符串，包括空字符串
 *
 * 输入:
 * s = "aa"
 * p = "a"
 * 输出: false
 *
 * @author caoyang
 */
public class Leetcode44 {
    public static boolean isMatch(String s, String p) {
        int m = s.length();
        int n = p.length();
        char[] chars1 = s.toCharArray();
        char[] chars2 = p.toCharArray();
        boolean[][] dp = new boolean[m+1][n+1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 && j == 0){
                    dp[i][j] = true;
                    continue;
                }
                if (j == 0){
                    dp[i][j] = false;
                    continue;
                }
                if (chars2[j-1] != '*'){
                    if (i > 0 && (chars1[i-1] == chars2[j-1] || chars2[j-1] == '?')){
                        dp[i][j] |= dp[i-1][j-1];
                    }
                } else {
                    // 当 p[j-1]='*'，即 s[0:i-1] 与 p[0:j]是否匹配，或s[0:i] 与 p[0:j-1]是否匹配
                    dp[i][j] |= dp[i][j-1];
                    if (i > 0){ dp[i][j] |= dp[i-1][j]; }
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        String s = "aa";
        String p = "a";
        boolean res = isMatch(s, p);
        System.out.println(res);
    }
}
