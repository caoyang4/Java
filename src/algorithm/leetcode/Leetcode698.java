package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 698. 划分为k个相等的子集
 * 给定一个整数数组  nums 和一个正整数 k，找出是否有可能把这个数组分成 k 个非空子集，其总和都相等
 * 输入： nums = [4, 3, 2, 3, 5, 2, 1], k = 4
 * 输出： True
 * 说明： 有可能将其分成 4 个子集（5），（1,4），（2,3），（2,3）等于总和
 *
 * @author caoyang
 */
public class Leetcode698 {
    public static boolean canPartitionKSubsets(int[] nums, int k) {
        int sum = Arrays.stream(nums).sum();
        int n = nums.length;
        if (n < k || sum % k != 0) return false;
        int avg = sum / k;
        Arrays.sort(nums);
        boolean[] used = new boolean[n];
        return trackBack(nums, used, k, avg, 0, 0);
    }
    public static boolean trackBack(int[] nums, boolean[] used, int k, int target, int subSum, int start){
        if (k == 1) return true;
        if (target == subSum){
            // 继续找下一组
            return trackBack(nums, used, k-1, target, 0, 0);
        }
        for (int i = start; i < nums.length; i++) {
            if (used[i]) continue;
            // 剪枝
            if (i > 0 && nums[i-1] == nums[i] && !used[i-1]) continue;
            used[i] = true;
            if (trackBack(nums, used, k, target, subSum+nums[i],i+1)){
                return true;
            }
            used[i] = false;
        }
        return false;
    }

    public static void main(String[] args) {
        int [] nums = {1,1,1,1,2,2,2,2};
        int k = 2;
        boolean result = canPartitionKSubsets(nums, k);
        System.out.println(result);
    }
}
