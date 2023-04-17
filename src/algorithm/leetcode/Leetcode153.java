package src.algorithm.leetcode;

/**
 * 153. 寻找旋转排序数组中的最小值
 * 给你一个元素值互不相同的数组 nums
 * 输入：nums = [3,4,5,1,2]
 * 输出：1
 * @author caoyang
 */
public class Leetcode153 {
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
        int[] nums = {3,4,5,1,2};
        int result = findMin(nums);
        System.out.println(result);
    }
}
