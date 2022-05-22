package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 462. 最少移动次数使数组元素相等 II
 * 输入：nums = [1,10,2,9]
 * 输出：16
 * @author caoyang
 */
public class Leetcode462 {
    public static int minMoves2(int[] nums) {
        int len = nums.length;
        if (len == 1){return 0;}
        Arrays.sort(nums);
        int middle = len >> 1;
        int step = 0;
        for (int num : nums) {
            step += Math.abs(nums[middle] - num);
        }
        return step;
    }

    public static void main(String[] args) {
        int[] nums = {1,10,2,9};
        int result = minMoves2(nums);
        System.out.println(result);
    }
}
