package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 16. 最接近的三数之和
 *
 * 给你一个长度为 n 的整数数组 nums 和 一个目标值 target。请你从 nums 中选出三个整数，使它们的和与 target 最接近。
 * 返回这三个数的和。
 * 假定每组输入只存在恰好一个解。
 *
 * 输入：nums = [-1,2,1,-4], target = 1
 * 输出：2
 * 解释：与 target 最接近的和是 2 (-1 + 2 + 1 = 2)
 *
 * @author caoyang
 */
public class Leetcode16 {
    public static int threeSumClosest(int[] nums, int target) {
        if(nums == null || nums.length < 3){ return 0; }
        Arrays.sort(nums);
        int res = nums[0] + nums[1] + nums[2];
        for (int i = 0; i < nums.length-2; i++) {
            int curr = nums[i];
            int left = i + 1;
            int right = nums.length - 1;
            while (left < right){
                int r = curr + nums[left] + nums[right];
                if(Math.abs(r-target) < Math.abs(res-target)){
                    res = r;
                }
                if(r < target){
                    left++;
                }else if(r > target){
                    right--;
                }else {
                    return r;
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        int[] nums = {0,2,1,-3};
        int target = 1;
        int res = threeSumClosest(nums, target);
        System.out.println(res);
    }
}
