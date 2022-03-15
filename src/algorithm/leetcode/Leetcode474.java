package src.algorithm.leetcode;

/**
 * 474. 一和零
 * 给你一个二进制字符串数组 strs 和两个整数 m 和 n 。
 * 请你找出并返回 strs 的最大子集的长度，该子集中 最多 有 m 个 0 和 n 个 1 。
 * 如果 x 的所有元素也是 y 的元素，集合 x 是集合 y 的 子集
 *
 * 输入：strs = ["10", "0001", "111001", "1", "0"], m = 5, n = 3
 * 输出：4
 *
 * @author caoyang
 */
public class Leetcode474 {
    public static int findMaxForm(String[] strs, int m, int n) {
        int l = strs.length;
        int[] zeros = new int[l];
        int[] ones = new int[l];
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < strs[i].length(); j++) {
                if(strs[i].charAt(j) == '0'){
                    zeros[i]++;
                } else {
                    ones[i]++;
                }
            }
        }
        // dp[i][j][k]表示 j个0和k个1 最多组成多少个前i个数组的子串
        int[][][] dp = new int[l+1][m+1][n+1];
        for (int i = 1; i <= l; i++) {
            for (int j = 0; j <= m; j++) {
                for (int k = 0; k <= n; k++) {
                    dp[i][j][k] = dp[i-1][j][k];
                    if(j >= zeros[i-1] && k >= ones[i-1]) {
                        dp[i][j][k] = Math.max(dp[i][j][k], dp[i-1][j-zeros[i-1]][k-ones[i-1]]+1);
                    }
                }
            }
        }
        return dp[l][m][n];
    }


    public static void main(String[] args) {
        String[] strs = {"10", "0001", "111001", "1", "0"};
        int m = 5;
        int n = 3;
        int res = findMaxForm(strs, m, n);
        System.out.println(res);
    }
}
