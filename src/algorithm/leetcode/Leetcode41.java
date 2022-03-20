package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 41. 缺失的第一个正数
 * 给你一个未排序的整数数组 nums ，请你找出其中没有出现的最小的正整数。
 * 请你实现时间复杂度为 O(n) 并且只使用常数级别额外空间的解决方案
 *
 * 输入：nums = [1,2,0]
 * 输出：3
 *
 * @author caoyang
 */
public class Leetcode41 {
    /**
     * 排序+遍历
     * O(N) + O(1)
     * @param nums
     * @return
     */
    public static int firstMissingPositive(int[] nums) {
        Arrays.sort(nums);
        if (nums[0] > 1 || nums[nums.length-1] <= 0){
            return 1;
        } else if(nums[nums.length-1] == 1){
            return 2;
        }
        int tmp = nums[0];
        for (int i = 1; i < nums.length; i++) {
            if(nums[i] > 0){
                if(nums[i-1] <= 0){
                    if(nums[i] > 1){
                        return 1;
                    } else {
                        tmp = 2;
                    }
                } else {
                    if(tmp < nums[i]){
                        if( tmp != nums[i-1]){
                            return tmp;
                        }else {
                            tmp++;
                        }
                    }
                }
            }
        }
        return tmp == nums[nums.length-1] ? ++tmp : tmp;
    }

    public static void main(String[] args) {
        int[] nums = {1,3};
        int res = firstMissingPositive(nums);
        System.out.println(res);
    }
}
