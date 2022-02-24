package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 18. 四数之和
 * 给你一个由 n 个整数组成的数组 nums ，和一个目标值 target 。
 * 请你找出并返回满足下述全部条件且不重复的四元组 [nums[a], nums[b], nums[c], nums[d]]
 * nums[a] + nums[b] + nums[c] + nums[d] == target
 * 你可以按 任意顺序 返回答案
 *
 * 输入：nums = [1,0,-1,0,-2,2], target = 0
 * 输出：[[-2,-1,1,2],[-2,0,0,2],[-1,0,0,1]]
 *
 * @author caoyang
 */
public class Leetcode18 {
    /**
     * 四数和
     * @param nums
     * @param target
     * @return
     */
    public static List<List<Integer>> fourSum(int[] nums, int target) {
        List<List<Integer>> result = new ArrayList<>();
        if (nums == null || nums.length < 4) {
            return result;
        }
        Arrays.sort(nums);
        for (int i = 0; i < nums.length-3; i++) {
            if(i > 0 && nums[i] == nums[i-1]){ continue; }
            int curr1 = nums[i];
            for (int j = i+1; j < nums.length-2; j++) {
                if(j > i+1 && nums[j] == nums[j-1]){ continue; }
                int curr2 = nums[j];
                int left = j+1;
                int right = nums.length - 1;
                while (left < right){
                    int leftV = nums[left];
                    int rightV = nums[right];
                    int r = curr1 + curr2 + nums[left] + nums[right];
                    if (r < target){
                        while (left < right && nums[left] == leftV){
                            left++;
                        }
                    }else if(r > target){
                        while (left < right && nums[right] == rightV){
                            right--;
                        }
                    }else {
                        List<Integer> list = new ArrayList<>();
                        list.add(curr1);
                        list.add(curr2);
                        list.add(nums[left]);
                        list.add(nums[right]);
                        result.add(list);
                        while (left < right && nums[left] == leftV){
                            left++;
                        }
                        while (left < right && nums[right] == rightV){
                            right--;
                        }
                    }
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {1,0,-1,0,-2,2};
        int target = 0;
//        List<List<Integer>> result = fourSum(nums, target);
        Arrays.sort(nums);
        List<List<Integer>> result = nSum(nums, target, 0, 4);
        for (List<Integer> integers : result) {
            for (Integer integer : integers) {
                System.out.print(integer + "\t");
            }
            System.out.println();
        }
    }

    /**
     * n数之和api
     * nums数组已经排序
     * @param nums
     * @param target
     * @return
     */
    public static List<List<Integer>> nSum(int[] nums, int target, int start, int n) {
        List<List<Integer>> result = new ArrayList<>();
        int size = nums.length;
        if(n < 2 || size < n){
            return result;
        }
        if (n == 2) {
            int left = start;
            int right = size - 1;
            while (left < right){
                int r = nums[left] + nums[right];
                int leftV = nums[left];
                int rightV = nums[right];
                if (r < target) {
                    // 左去重
                    while (left < right && nums[left] == leftV){
                        left++;
                    }
                } else if (r > target){
                    // 右去重
                    while (left < right && nums[right] == rightV){
                        right--;
                    }
                } else {
                    List<Integer> integers = new ArrayList<>();
                    integers.add(nums[left]);
                    integers.add(nums[right]);
                    result.add(integers);
                    // 左右去重
                    while (left < right && nums[left] == leftV){
                        left++;
                    }
                    while (left < right && nums[right] == rightV){
                        right--;
                    }
                }
            }
        } else {
            for (int i = start; i < size; i++) {
                // 递归
                List<List<Integer>> subResult = nSum(nums, target - nums[i], i+1, n-1);
                for (List<Integer> list : subResult) {
                    list.add(nums[i]);
                    List<Integer> integers = new ArrayList<>();
                    integers.addAll(list);
                    result.add(integers);
                }

                //去重
                while (i < size - 1 && nums[i + 1] == nums[i]) {
                    i++;
                }
            }
        }
        return result;
    }
}
