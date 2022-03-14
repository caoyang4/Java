package src.algorithm.leetcode;

/**
 * 97. 交错字符串
 * 给定三个字符串s1、s2、s3，请你帮忙验证s3是否是由s1和s2交错组成的。
 * 两个字符串 s 和 t 交错 的定义与过程如下，其中每个字符串都会被分割成若干非空子字符串：
 *
 * @author caoyang
 */
public class Leetcode97 {
    public static boolean isInterleave(String s1, String s2, String s3) {
        if(s1.length() + s2.length() != s3.length()){
            return false;
        }
        char[] chars1 = s1.toCharArray();
        char[] chars2 = s2.toCharArray();
        char[] chars3 = s3.toCharArray();
        int m = chars1.length, n = chars2.length;
        // dp[i][j] 表示s3[0, i+j] 是否由 s1[0,i] 和 s2[0,j] 组合而成
        boolean[][] dp = new boolean[m+1][n+1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 && j == 0){
                    dp[i][j] = true;
                    continue;
                }
                if (i > 0 && chars1[i-1] == chars3[i+j-1]){
                    dp[i][j] |= dp[i-1][j];
                }
                if (j > 0 && chars2[j-1] == chars3[i+j-1]){
                    dp[i][j] |= dp[i][j-1];
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        String s1 = "db";
        String s2 = "b";
        String s3 = "cbb";
        boolean res = isInterleave(s1, s2, s3);
        System.out.println(res);
    }
}
