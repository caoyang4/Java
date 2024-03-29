package src.algorithm.leetcode;

/**
 * 55. 跳跃游戏
 * 给定一个非负整数数组 nums ，你最初位于数组的 第一个下标 。
 * 数组中的每个元素代表你在该位置可以跳跃的最大长度。
 * 判断你是否能够到达最后一个下标
 *
 * 输入：nums = [2,3,1,1,4]
 * 输出：true
 * 解释：可以先跳 1 步，从下标 0 到达下标 1, 然后再从下标 1 跳 3 步到达最后一个下标。
 *
 * @author caoyang
 */
public class Leetcode55 {
    public static boolean canJump(int[] nums) {
        boolean[] f = new boolean[nums.length];
        f[0] = true;
        for (int i = 1; i < nums.length; i++) {
            f[i] = false;
            for (int j = 0; j < i; j++) {
                if(i - j <= nums[j] && f[j]){
                    f[i] = true;
                    break;
                }
            }
        }
        return f[nums.length - 1];
    }

    public static void main(String[] args) {
        int[] nums = {2,3,1,1,4};
        boolean res = canJump(nums);
        System.out.println(res);
    }
}
