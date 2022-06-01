package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 611. 有效三角形的个数
 * 输入: nums = [4,2,3,4]
 * 输出: 4
 * @author caoyang
 */
public class Leetcode611 {
    // 排序+双指针
    public static int triangleNumber(int[] nums) {
        Arrays.sort(nums);
        int count = 0;
        int n = nums.length;
        for (int i = n-1; i > 1; i--) {
            int l = 0;
            int r = i - 1;
            while (l < r){
                if (nums[l] + nums[r] > nums[i]){
                    count += r-l;
                    r--;
                } else {
                    l++;
                }
            }
        }
        return count;
    }
    // 暴力遍历
    public static int triangleNumber1(int[] nums) {
        int n = nums.length;
        int count = 0;
        for (int i = 0; i < n-2; i++) {
            for (int j = i+1; j < n-1; j++) {
                for (int k = j+1; k < n; k++) {
                    if (nums[i]+nums[j] > nums[k] && nums[i]+nums[k] > nums[j] && nums[k]+nums[j] > nums[i]){
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static void main(String[] args) {
        int[] nums = {4,2,3,4};
        int result = triangleNumber(nums);
        System.out.println(result);
    }
}
