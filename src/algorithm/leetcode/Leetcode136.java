package src.algorithm.leetcode;

/**
 * 136. 只出现一次的数字
 *
 * 输入: [4,1,2,1,2]
 * 输出: 4
 * @author caoyang
 */
public class Leetcode136 {
    public static int singleNumber(int[] nums) {
        for (int i = 1; i < nums.length; i++) {
            nums[i] ^= nums[i-1];
        }
        return nums[nums.length-1];
    }

    public static void main(String[] args) {
        int[] nums = {4,1,2,1,2};
        int result = singleNumber(nums);
        System.out.println(result);
    }
}
