package src.algorithm.targetOffer;

/**
 * 95. 最长公共子序列
 * 给定两个字符串text1 和text2，返回这两个字符串的最长公共子序列的长度。如果不存在公共子序列 ，返回 0 。
 * 一个字符串的子序列是指这样一个新的字符串：它是由原字符串在不改变字符的相对顺序的情况下删除某些字符（也可以不删除任何字符）后组成的新字符串
 *
 * 输入：text1 = "abcde", text2 = "ace"
 * 输出：3
 * 解释：最长公共子序列是 "ace" ，它的长度为 3
 *
 * @author caoyang
 */
public class TargetOffer95 {
    public static int longestCommonSubsequence(String text1, String text2) {
        if (text1 == null || text2 == null || text1.length() == 0 || text2.length() == 0){
            return 0;
        }
        char[] chars1 = text1.toCharArray();
        int m = chars1.length;
        char[] chars2 = text2.toCharArray();
        int n = chars2.length;
        // dp[i][j] 表示 text1 中前 i 个字符和 text2 中前 j 个字符的最长公共子串长度
        int[][] dp = new int[m+1][n+1];
        // 如果要返回最长子串，需要记载中间过程，然后回溯
        int[][] pai = new int[m+1][n+1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if(i==0 || j==0){
                    dp[i][j] = 0;
                } else {
                    dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                    pai[i][j] = dp[i][j] == dp[i-1][j] ? 1 : 2;
                    if (chars1[i-1] == chars2[j-1]){
                        dp[i][j] = Math.max(dp[i][j], dp[i-1][j-1]+1);
                        pai[i][j] = dp[i][j] == (dp[i-1][j-1]+1) ? 3 : pai[i][j];
                    }
                }
            }
        }
        char[] res = new char[dp[m][n]];
        int i = m, j = n;
        int l = dp[m][n] - 1;
        while (i > 0 && j > 0){
            if(pai[i][j] == 1){
                --i;
            }else if (pai[i][j] == 2){
                --j;
            } else if (pai[i][j] == 3){
                res[l--] = chars1[i-1];
                --i;
                --j;
            }
        }
        String s = String.valueOf(res);
        System.out.println(s);
        return dp[m][n];
    }

    public static void main(String[] args) {
        String text1 = "abcde";
        String text2 = "ace";
        int res = longestCommonSubsequence(text1, text2);
        System.out.println(res);
    }
}
