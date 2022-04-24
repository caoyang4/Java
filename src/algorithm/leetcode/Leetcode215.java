package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 215. 数组中的第K个最大元素
 * 输入: [3,2,1,5,6,4] 和 k = 2
 * 输出: 5
 * @author caoyang
 */
public class Leetcode215 {
    public static int findKthLargest(int[] nums, int k) {
        int n = nums.length;
        for (int i = n-1; i >= 1; i--) {
            for (int j = 0; j < i; j++) {
                if(nums[j] >= nums[j+1]){
                    swap(nums, j, j+1);
                }
            }
            if(i == n-k){
                return nums[n-k];
            }
        }
        return nums[n-k];
    }
    public static void swap(int[] nums, int i, int j){
        if(i==j){return;}
        nums[i] =  nums[i]^ nums[j];
        nums[j] =  nums[i]^ nums[j];
        nums[i] =  nums[i]^ nums[j];
    }

    public static void main(String[] args) {
        int[] nums = {3,2,1,5,6,4};
        int k = 2;
        int result = findKthLargest(nums, k);
        System.out.println(result);
    }
}
