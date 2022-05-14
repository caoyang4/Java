package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 410. 分割数组的最大值
 * 给定一个非负整数数组 nums 和一个整数 m ，你需要将这个数组分成 m 个非空的连续子数组。
 * 设计一个算法使得这 m 个子数组各自和的最大值最小
 * 输入：nums = [7,2,5,10,8], m = 2
 * 输出：18
 * 解释：其中最好的方式是将其分为 [7,2,5] 和 [10,8]
 *
 * @author caoyang
 */
public class Leetcode410 {
    public static int splitArray(int[] nums, int m) {
        int n = nums.length;
        // 转移方程：dp[i][j] = min{dp[i][j], max{dp[k][j-1], sum[i] - sum[k]}}
        int[][] dp = new int[n][m+1];

        int[] sum = new int[n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(dp[i], Integer.MAX_VALUE);
            sum[i] = nums[i];
            if(i > 0){sum[i] += sum[i-1];}
            dp[i][1] = sum[i];
        }
        for (int i = 0; i < n; i++) {
            for (int j = 2; j <= Math.min(i+1, m); j++) {
                for (int k = 0; k < i; k++) {
                    if(dp[k][j-1] == Integer.MAX_VALUE){continue;}
                    dp[i][j] = Math.min(dp[i][j], Math.max(dp[k][j-1], sum[i]-sum[k]));
                }
            }
        }
        return dp[n-1][m];
    }

    public static void main(String[] args) {
        int[] nums = {0,8,1,4};
        int m = 4;
        int reuslt = splitArray(nums, m);
        System.out.println(reuslt);
    }
}
