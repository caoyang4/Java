package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 128. 最长连续序列
 * 给定一个未排序的整数数组 nums ，找出数字连续的最长序列（不要求序列元素在原数组中连续）的长度
 * @author caoyang
 */
public class Leetcode128 {
    public static int longestConsecutive(int[] nums) {
        if(nums == null || nums.length == 0) {
            return 0;
        }
        // nlog(n)
        Arrays.sort(nums);
        int max = 1;
        int tmp = max;
        for (int i = 1; i < nums.length; i++) {
            if(nums[i] == nums[i-1] + 1){
                tmp++;
                max = Math.max(max, tmp);
            }else if(nums[i] - nums[i-1] > 1){
                tmp = 1;
            }
        }
        return max;
    }

    public static int longestConsecutive1(int[] nums) {
        Set<Integer> set = new HashSet<>();
        for (int num : nums) {
            set.add(num);
        }
        int max = 0;
        for (int num : set) {
            if(!set.contains(num-1)){
                int tmp = 0;
                while (set.contains(num)){
                    num++;
                    tmp++;
                }
                max = Math.max(max, tmp);
            }
        }
        return max;
    }


    public static void main(String[] args) {
        int[] nums = {100,4,200,1,3,2};
        int result = longestConsecutive1(nums);
        System.out.println(result);
    }
}
