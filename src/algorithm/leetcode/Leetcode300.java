package src.algorithm.leetcode;

/**
 * 300. 最长递增子序列
 * 给你一个整数数组 nums ，找到其中最长严格递增子序列的长度。
 * 子序列是由数组派生而来的序列，删除（或不删除）数组中的元素而不改变其余元素的顺序。
 * 例如，[3,6,2,7] 是数组 [0,3,1,6,2,2,7] 的子序列
 *
 * 输入：nums = [10,9,2,5,3,7,101,18]
 * 输出：4
 * 解释：最长递增子序列是 [2,3,7,101]，因此长度为 4
 *
 * @author caoyang
 */
public class Leetcode300 {
    public static int lengthOfLIS(int[] nums) {
        if(nums == null || nums.length < 1){
            return 0;
        }
        int[] f = new int[nums.length];
        int res = 0;
        for (int i = 0; i < nums.length; i++) {
            f[i] = 1;
            for (int j = 0; j < i; j++) {
                if(nums[j] < nums[i]){
                    f[i] = Math.max(f[i], f[j] + 1);
                }
            }
            res = Math.max(res, f[i]);
        }
        return res;
    }

    public static void main(String[] args) {
        int[] nums = {10,9,2,5,3,7,101,18};
        int res = lengthOfLIS(nums);
        System.out.println(res);
    }
}
