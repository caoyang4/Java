package src.algorithm.leetcode;

/**
 * 45. 跳跃游戏 II
 * 给你一个非负整数数组nums ，你最初位于数组的第一个位置。
 * 数组中的每个元素代表你在该位置可以跳跃的最大长度。
 * 你的目标是使用【最少的跳跃次数】到达数组的最后一个位置
 * 假设你总是可以到达数组的最后一个位置
 *
 * 输入: nums = [2,3,1,1,4]
 * 输出: 2
 *
 * @author caoyang
 */
public class Leetcode45 {
    public static int jump(int[] nums) {
        if(nums.length <= 1){
            return 0;
        }
        int len = nums.length;
        int[] dp = new int[len];
        dp[0] = 0;
        for (int i = 1; i < len; i++) {
            dp[i] = Integer.MAX_VALUE;
            for (int j = i-1; j >= 0; j--) {
                if(nums[j] >= (i-j)){
                    dp[i] = Math.min(dp[i], dp[j]+1);
                }
            }
        }
        return dp[len-1];
    }

    public static void main(String[] args) {
        int[] nums = {2,3,1,1,4};
        int res = jump(nums);
        System.out.println(res);
    }
}
