package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 628. 三个数的最大乘积
 *
 * 输入：nums = [1,2,3]
 * 输出：6
 * @author caoyang
 */
public class Leetcode628 {
    public static int maximumProduct(int[] nums) {
        int n = nums.length;
        Arrays.sort(nums);
        int max = nums[n-1] * nums[n-2] * nums[n-3];
        if (nums[1] >= 0 ){
            return max;
        } else {
            int tmp = nums[0] * nums[1] * nums[n-1];
            max = Math.max(max, tmp);
        }
        return max;
    }

    public static void main(String[] args) {
        int[] nums = {1,2,3,4};
        int result = maximumProduct(nums);
        System.out.println(result);
    }
}
