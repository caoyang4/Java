package src.algorithm.leetcode;

/**
 * 343. 整数拆分
 * 给定一个正整数 n ，将其拆分为 k 个 正整数 的和（ k >= 2 ），并使这些整数的乘积最大化
 *
 * 输入: n = 10
 * 输出: 36
 * 解释: 10 = 3 + 3 + 4, 3 × 3 × 4 = 36
 * @author caoyang
 */
public class Leetcode343 {
    public static int integerBreak(int n) {
        int[] dp = new int[n+1];
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            dp[i] = dp[i-1];
            for (int j = i-1; j >= 1; j--) {
                dp[i] = Math.max(dp[i], j*Math.max(i-j, dp[i-j]));
            }
        }
        return dp[n];
    }
    public static void main(String[] args) {
        int n = 10;
        int result = integerBreak(n);
        System.out.println(result);
    }
}
