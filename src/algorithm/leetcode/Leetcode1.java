package src.algorithm.leetcode;

import src.algorithm.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. 两数之和
 * 给定一个整数数组 nums 和一个整数目标值 target，
 * 请你在该数组中找出 和为目标值 target  的两个整数，并返回它们的数组下标。
 * @author caoyang
 */
public class Leetcode1 {
    /**
     * O(N^2)
     */
    public static int[] twoSum1(int[] nums, int target) {
        int [] res = new int[2];
        for (int i = 0; i < nums.length - 1; i++) {
            for (int j = i+1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target){
                    res[0] = i;
                    res[1] = j;
                    return res;
                }
            }
        }
        return res;
    }

    public static int[] twoSum2(int[] nums, int target) {
        int [] res = new int[2];
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            if(map.containsKey(nums[i])){
                res[0] = map.get(nums[i]);
                res[1] = i;
                return res;
            }
            map.put(target-nums[i], i);
        }

        return res;
    }



    public static void main(String[] args) {
        int[] nums = {2, 4, 5};
        CommonUtils.printArray(twoSum1(nums, 7));
        CommonUtils.printArray(twoSum2(nums, 7));
    }

}
