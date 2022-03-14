package src.algorithm.leetcode;

/**
 * 72. 编辑距离
 *
 * 给你两个单词word1和word2， 请返回将word1转换成word2所使用的最少操作数。
 * 你可以对一个单词进行如下三种操作：
 * 插入一个字符
 * 删除一个字符
 * 替换一个字符
 *
 * @author caoyang
 */
public class Leetcode72 {
    public static int minDistance(String word1, String word2) {
        char[] chars1 = word1.toCharArray();
        char[] chars2 = word2.toCharArray();
        int m = chars1.length, n = chars2.length;
        // dp[i][j] 表示 word1[0:i] 变为 word2[0:j] 的最小编辑距离
        int[][] dp = new int[m+1][n+1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0){
                    dp[i][j] = j;
                }
                if (j == 0){
                    dp[i][j] = i;
                }
                if (i > 0 && j > 0){
                    // 添加或删除
                    dp[i][j] = Math.min(dp[i][j-1]+1, dp[i-1][j]+1);
                    // 替换
                    dp[i][j] = Math.min(dp[i][j], dp[i-1][j-1]+1);
                    // 相等
                    if (chars1[i-1] == chars2[j-1]){
                        dp[i][j] = Math.min(dp[i][j], dp[i-1][j-1]);
                    }
                }
            }
        }
        return dp[m][n];
    }

    public static void main(String[] args) {
        String word1 = "horse";
        String word2 = "ros";
        int res = minDistance(word1, word2);
        System.out.println(res);
    }
}
