package src.algorithm.leetcode;

import java.util.*;

/**
 * 15. 三数之和
 * 给你一个包含 n 个整数的数组nums，判断nums中是否存在三个元素 a，b，c ，使得a + b + c = 0
 * 请你找出所有和为 0 且不重复的三元组。
 * 注意：答案中不可以包含重复的三元组。
 *
 * 输入：nums = [-1,0,1,2,-1,-4]
 * 输出：[[-1,-1,2],[-1,0,1]]
 *
 * @author caoyang
 */
public class Leetcode15 {
    /**
     * 排序+双指针
     * @param nums
     * @return
     */
    public static List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        if (nums == null || nums.length < 3) {
            return res;
        }
        Arrays.sort(nums);

        for (int i = 0; i < nums.length; i++) {
            // 若 nums[i]>0nums[i]>0，因为已经排序好，所以后面不可能有三个数加和等于 00，直接返回结果
            if(nums[i] > 0){
                return res;
            }
            // 重复元素，直接跳过
            if(i > 0 && nums[i] == nums[i-1]){
                continue;
            }

            int curr = nums[i];
            int left = i + 1;
            int right = nums.length - 1;
            while (left < right){
                int r = curr + nums[left] + nums[right];
                if(r == 0){
                    List<Integer> list = new ArrayList<>();
                    list.add(curr);
                    list.add(nums[left]);
                    list.add(nums[right]);
                    res.add(list);
                    // 判断左界和右界是否和下一位置重复，去除重复解
                    while (left < right && nums[left] == nums[left+1]){
                        left++;
                    }
                    while (left < right && nums[right] == nums[right-1]){
                        right--;
                    }
                    // 将 L,R 移到下一位置，寻找新的解
                    left++;
                    right--;
                } else if (r < 0){
                    // r < 0，left向右移动，使 r 变大
                    left++;
                } else {
                    // r > 0，right向左移动，使 r 变小
                    right--;
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        int[] nums = {-4,-2,1,-5,-4,-4,4,-2,0,4,0,-2,3,1,-5,0};
        List<List<Integer>> res = threeSum(nums);
        for (List<Integer> re : res) {
            for (Integer integer : re) {
                System.out.print(integer + "\t");
            }
            System.out.println();
        }

    }
}
