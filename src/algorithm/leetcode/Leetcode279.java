package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 279. 完全平方数
 * 给你一个整数 n ，返回 和为 n 的完全平方数的最少数量
 *
 * 输入：n = 13
 * 输出：2
 * 解释：13 = 4 + 9
 * @author caoyang
 */
public class Leetcode279 {
    public static int numSquares(int n) {
        int[] dp = new int[n+1];
        dp[1] = 1;
        for (int i = 2; i <= n; i++) {
            if(isSquare(i)){
                dp[i] = 1;
            } else {
                dp[i] = i;
                for (int j = i-1; j >= i-j; j--) {
                    dp[i] = Math.min(dp[j]+dp[i-j], dp[i]);
                }
            }
        }
        System.out.println(Arrays.toString(dp));
        return dp[n];
    }
    public static boolean isSquare(int n){
        int m = (int) Math.sqrt(n);
        return n == m*m;
    }

    public static void main(String[] args) {
        int n = 8;
        int result = numSquares(n);
        System.out.println(result);
    }
}
