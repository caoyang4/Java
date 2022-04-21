package src.algorithm.leetcode;

/**
 * 198. 打家劫舍
 * 输入：[1,2,3,1]
 * 输出：4
 * @author caoyang
 */
public class Leetcode198 {
    public static int rob(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n+1];
        dp[0] = 0;
        dp[1] = nums[0];
        for (int i = 2; i <= n; i++) {
            dp[i] = Math.max(dp[i-1], dp[i-2]+nums[i-1]);
        }
        return dp[n];
    }

    public static void main(String[] args) {
        int[] nums = {1,2,3,1};
        int result = rob(nums);
        System.out.println(result);
    }
}
