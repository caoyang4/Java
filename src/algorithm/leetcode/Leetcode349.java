package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 349. 两个数组的交集
 * 输入：nums1 = [4,9,5], nums2 = [9,4,9,8,4]
 * 输出：[9,4]
 * @author caoyang
 */
public class Leetcode349 {
    public static int[] intersection(int[] nums1, int[] nums2) {
        Set<Integer> set = new HashSet<>();
        Set<Integer> set1 = new HashSet<>();
        Set<Integer> set2 = new HashSet<>();
        for (int num : nums1) {
            set.add(num);
            set1.add(num);
        }
        for (int num : nums2) {
            set.add(num);
            set2.add(num);
        }
        for (int num : set1) {
            if(!set2.contains(num)){
                set.remove(num);
            }
        }
        for (int num : set2) {
            if(!set1.contains(num)){
                set.remove(num);
            }
        }
        int[] ans = new int[set.size()];
        int index = 0;
        for (int num : set) {
            ans[index++] = num;
        }
        return ans;
    }

    public static void main(String[] args) {
        int[] nums1 = {4,9,5};
        int[] nums2 = {9,4,9,8,4};
        int[] result = intersection(nums1, nums2);
        System.out.println(Arrays.toString(result));
    }
}
