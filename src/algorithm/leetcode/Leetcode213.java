package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 213. 打家劫舍 II
 * 所有的房屋都围成一圈
 *
 * 输入：nums = [1,2,3,1]
 * 输出：4
 * @author caoyang
 */
public class Leetcode213 {
    public static int rob(int[] nums) {
        int n = nums.length;
        if (n == 0){return 0;}
        if (n == 1){return nums[0];}
        int[] dp = new int[n+1];
        // 从第 0 栋房子开始偷，即偷 0 - n-2 栋房子
        dp[1] = nums[0];
        for (int i = 2; i < n; i++) {
           dp[i] = Math.max(dp[i-1], dp[i-2]+nums[i-1]);
        }
        int max1 = dp[n-1];
        Arrays.fill(dp, 0);
        // 从第 1 栋房子开始偷，即偷 1 - n-1 栋房子
        for (int i = 2; i <= n; i++) {
            dp[i] = Math.max(dp[i-1], dp[i-2]+nums[i-1]);
        }
        return Math.max(max1, dp[n]);
    }

    public static void main(String[] args) {
        int[] nums = {2,3,2};
        int result = rob(nums);
        System.out.println(result);
    }
}
