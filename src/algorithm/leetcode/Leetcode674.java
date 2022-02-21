package src.algorithm.leetcode;

/**
 * 给定一个未经排序的整数数组，找到最长且 连续递增的子序列，并返回该序列的长度。
 * 连续递增的子序列 可以由两个下标 l 和 r（l < r）确定，如果对于每个 l <= i < r，都有 nums[i] < nums[i + 1] ，
 * 那么子序列 [nums[l], nums[l + 1], ..., nums[r - 1], nums[r]] 就是连续递增子序列。
 *
 * 输入：nums = [1,3,5,4,7]
 * 输出：3
 * 解释：最长连续递增序列是 [1,3,5], 长度为3。
 * 尽管 [1,3,5,7] 也是升序的子序列, 但它不是连续的，因为 5 和 7 在原数组里被 4 隔开。
 *
 * @author caoyang
 */
public class Leetcode674 {
    public static int findLengthOfLCIS(int[] nums) {
        int[] f = new int[nums.length];
        int max = f[0] = 1;
        for (int i = 1; i < nums.length; i++) {
            f[i] =  (nums[i] > nums[i-1] ? f[i-1] + 1 : 1);
            max = Math.max(f[i], max);
        }
        return max;
    }

    public static void main(String[] args) {
        int[] nums = {1,3,5,4,7};
        int res = findLengthOfLCIS(nums);
        System.out.println(res);
    }
}
