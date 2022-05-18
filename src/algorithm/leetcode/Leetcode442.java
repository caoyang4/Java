package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 442. 数组中重复的数据
 * 给你一个长度为 n 的整数数组 nums ，其中 nums 的所有整数都在范围 [1, n] 内，且每个整数出现一次或两次
 * 请你找出所有出现 两次 的整数，并以数组形式返回
 *
 * 输入：nums = [4,3,2,7,8,2,3,1]
 * 输出：[2,3]
 * @author caoyang
 */
public class Leetcode442 {
    public static List<Integer> findDuplicates(int[] nums) {
        List<Integer> result = new ArrayList<>();
        int n = nums.length;
        for (int i = 0; i < n; i++) {
            int x = Math.abs(nums[i]);
            if(nums[x-1] > 0){
                nums[x-1] = -nums[x-1];
            } else {
                result.add(x);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {4,3,2,7,8,2,3,1};
        List<Integer> result = findDuplicates(nums);
        System.out.println(result);
    }

}
