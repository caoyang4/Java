package src.algorithm.leetcode;

/**
 * 154. 寻找旋转排序数组中的最小值 II
 * 给你一个可能存在 重复 元素值的数组 nums
 * @author caoyang
 */
public class Leetcode154 {
    public static int findMin(int[] nums) {
        return search(nums, 0, nums.length-1);
    }
    public static int search(int[] nums, int start, int end){
        if(start == end || nums[start] < nums[end]){
            return nums[start];
        }
        int middle = start + ((end-start) >> 1);
        return Math.min(search(nums, start, middle), search(nums, middle+1, end));
    }

    public static void main(String[] args) {
        int[] nums = {3,1,3};
        int result = findMin(nums);
        System.out.println(result);
    }

}
