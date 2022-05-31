package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 560. 和为K的子数组
 * 输入：nums = [1,1,1], k = 2
 * 输出：2
 * @author caoyang
 */
public class Leetcode560 {
    // 前缀和
    public static int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        // 处理 sum(0,i) = k 的情况
        map.put(0, 1);
        int count = 0;
        int preSum = 0;
        for (int num : nums) {
            preSum += num;
            count += map.getOrDefault(preSum-k, 0);
            map.put(preSum, map.getOrDefault(preSum, 0) + 1);
        }
        return count;
    }
    // dfs 超时
    public static int subarraySum1(int[] nums, int k) {
        int count = 0;
        for (int i = 0; i < nums.length; i++) {
            count += trackBack(nums, i, k, k);
        }
        return count;
    }
    public static int trackBack(int[] nums, int start, int target, int origin){
        if (start >= nums.length) return 0;
        if (target == nums[start]) {
            return trackBack(nums, start+1, 0, 0) + 1;
        }
        return trackBack(nums, start+1, target-nums[start], target);
    }

    public static void main(String[] args) {
        int[] nums = {1,-1,0,0,0};
        int k = 0;
        int result = subarraySum(nums, k);
        System.out.println(result);
    }
}
