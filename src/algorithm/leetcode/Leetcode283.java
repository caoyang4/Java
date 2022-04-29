package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 283. 移动零
 *
 * 输入: nums = [0,1,0,3,12]
 * 输出: [1,3,12,0,0]
 * @author caoyang
 */
public class Leetcode283 {
    public static void moveZeroes(int[] nums) {
        int n = nums.length;
        int[] ans = new int[n];
        int index = 0;
        for (int i = 0; i < n; i++) {
            if(nums[i] != 0){
                ans[index++] = nums[i];
            }
        }
        for (int i = 0; i < n; i++) {
            nums[i] = ans[i];
        }

    }


    public static void main(String[] args) {
        int[] nums = {0,1,0,3,12};
        moveZeroes(nums);
        System.out.println(Arrays.toString(nums));
    }
}
