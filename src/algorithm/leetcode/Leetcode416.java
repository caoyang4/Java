package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 416. 分割等和子集
 * 给你一个只包含正整数的非空数组nums 。请你判断是否可以将这个数组分割成两个子集，使得两个子集的元素和相等
 * 输入：nums = [1,5,11,5]
 * 输出：true
 * @author caoyang
 */
public class Leetcode416 {
    // 元素非无限的背包问题
    public static boolean canPartition(int[] nums) {
        int sum = Arrays.stream(nums).sum();
        if ((sum & 1) != 0){
            return false;
        }
        int len = nums.length;
        int half = sum >> 1;
        // dp[i][j] 表示前 i 个数的和是否等于 j
        boolean[][] dp = new boolean[len+1][half+1];
        dp[0][0] = true;
        for (int i = 1; i <= len; i++) {
            for (int j = 0; j <= half; j++) {
                dp[i][j] = dp[i-1][j];
                if (nums[i-1] <= j){
                    dp[i][j] = dp[i][j] || dp[i-1][j-nums[i-1]];
                }
            }
        }
        return dp[len][half];
    }


    public static void main(String[] args) {
        int[] nums = {1,11,5,5,2};
        boolean result = canPartition(nums);
        System.out.println(result);
    }
}
