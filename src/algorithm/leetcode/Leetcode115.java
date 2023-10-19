package src.algorithm.leetcode;

/**
 * 115. 不同的子序列
 * 给定一个字符串 s 和一个字符串 t ，计算在 s 的子序列中 t 出现的个数。
 * 字符串的一个子序列是指，通过删除一些（也可以不删除）字符且不干扰剩余字符相对位置所组成的新字符串。
 * （例如，"ACE"是"ABCDE"的一个子序列，而"AEC"不是）
 *
 * 输入：s = "rabbbit", t = "rabbit"
 * 输出：3
 *
 * @author caoyang
 */
public class Leetcode115 {
    public static int numDistinct(String s, String t) {
        int m = s.length();
        int n = t.length();
        if(m < n){
            return 0;
        }
        char[] chars1 = s.toCharArray();
        char[] chars2 = t.toCharArray();
        // dp[i][j]表示 t[0:j] 在 s[0:i] 中出现的次数
        int[][] dp = new int[m+1][n+1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                // t空串边界，相当于删成空串
                if(j == 0){
                    dp[i][j] = 1;
                    continue;
                }
                // s空串边界
                if(i == 0){
                    dp[i][j] = 0;
                    continue;
                }
                if(i > 0 && j > 0){
                    dp[i][j] = dp[i-1][j];
                    if(chars1[i-1] == chars2[j-1]){
                        dp[i][j] += dp[i-1][j-1];
                    }
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        String s = "rabbbit";
        String t = "rabbit";
        int res = numDistinct(s, t);
        System.out.println(res);
    }
}
