package src.algorithm.leetcode;


/**
 * 494. 目标和
 * 给你一个整数数组 nums 和一个整数 target 。
 * 向数组中的每个整数前添加 '+' 或 '-' ，然后串联起所有整数，可以构造一个表达式
 *
 * 输入：nums = [1,1,1,1,1], target = 3
 * 输出：5
 * 解释：一共有 5 种方法让最终目标和为 3 。
 *     -1 + 1 + 1 + 1 + 1 = 3
 *     +1 - 1 + 1 + 1 + 1 = 3
 *     +1 + 1 - 1 + 1 + 1 = 3
 *     +1 + 1 + 1 - 1 + 1 = 3
 *     +1 + 1 + 1 + 1 - 1 = 3
 *
 * @author caoyang
 */
public class Leetcode494 {
    public static int findTargetSumWays(int[] nums, int target) {
        int sum = 0;
        for (int num : nums) sum += Math.abs(num);
        int n = nums.length;
        if (Math.abs(target) > sum) return 0;
        // f[i][j]=f[i−1][j−nums[i−1]]+f[i−1][j+nums[i−1]]
        int[][] dp = new int[n+1][2*sum+1];
        // 前 0 个数的和为 0 的组合数位 1
        dp[0][sum]= 1;
        for (int i = 1; i <= n; i++) {
            int x = nums[i-1];
            for (int j = -sum; j <= sum; j++) {
                if (j - x + sum >= 0) dp[i][j+sum] += dp[i-1][j-x+sum];
                if (j + x <= sum) dp[i][j+sum] += dp[i-1][j+x+sum];
            }
        }
        return dp[n][target+sum];
    }

    public static void main(String[] args) {
        int[] nums = {1,1,1,1,1};
        int target = 3;
        int result = findTargetSumWays(nums, target);
        System.out.println(result);
    }
}
