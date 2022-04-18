package src.algorithm.leetcode;

/**
 * 162. 寻找峰值
 * 峰值元素是指其值严格大于左右相邻值的元素，可以假设 nums[-1] = nums[n] = -∞
 * 输入：nums = [1,2,1,3,5,6,4]
 * 输出：1 或 5
 * @author caoyang
 */
public class Leetcode162 {
    public static int findPeakElement(int[] nums) {
        int start = 0;
        int end = nums.length - 1;
        while (start < end){
            int middle = start + ((end - start) >> 1);
            if (nums[middle] > nums[middle+1]){
                end = middle;
            } else {
                start = middle+1;
            }
        }
        return start;
    }

    public static void main(String[] args) {
        int[] nums = {1,2,1,3,5,6,4};
        int result = findPeakElement(nums);
        System.out.println(result);
    }
}
