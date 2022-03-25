package src.algorithm.leetcode;

/**
 * 377. 组合总和 Ⅳ
 * 给你一个由 不同 整数组成的数组 nums ，和一个目标整数 target 。请你从 nums 中找出并返回总和为 target 的元素组合的个数。
 * 注意：顺序不同的序列被视作不同的组合
 * 输入：nums = [1,2,3], target = 4
 * 输出：7
 * @author caoyang
 */
public class Leetcode377 {
    public static int combinationSum4(int[] nums, int target) {
        if(nums == null || nums.length == 0){ return 0; }
        // dp[n] = dp[n-A1] + ... + dp[n-Ax]
        int[] dp = new int[target+1];
        dp[0] = 1;
        for (int i = 1; i <= target; i++) {
            for (int j = 0; j < nums.length; j++) {
                if(i >= nums[j]){
                    dp[i] += dp[i - nums[j]];
                }
            }
        }
        return dp[target];
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3};
        int target = 4;
        int res = combinationSum4(nums, target);
        System.out.println(res);
    }
}
