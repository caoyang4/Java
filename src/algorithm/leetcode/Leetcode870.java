package src.algorithm.leetcode;

import src.algorithm.utils.SortUtils;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 给定两个大小相等的数组 nums1 和 nums2，
 * nums1 相对于 nums2 的优势可以用满足 nums1[i] > nums2[i] 的索引 i 的数目来描述。
 *
 * 返回 nums1 的任意排列，使其相对于 nums2 的优势最大化。
 * @author caoyang
 * @create 2023-05-10 17:20
 */
public class Leetcode870 {
    public static int[] advantageCount(int[] nums1, int[] nums2) {
        Arrays.sort(nums1);
        Integer[] idx = new Integer[nums2.length];
        for (int i = 0; i < idx.length; i++) {idx[i] = i;}
        Arrays.sort(idx, (i,j) -> nums2[i] - nums2[j]);
        int left = 0;
        int right = nums2.length - 1;
        for (int num : nums1) {
            if (num > nums2[idx[left]]){
                nums2[idx[left++]] = num;
            } else {
                nums2[idx[right--]] = num;
            }
        }
        return nums2;
    }

    public static void main(String[] args) {
        int[] nums1 = {2,7,11,15};
        int[] nums2 = {1,10,4,11};
        int[] option = advantageCount(nums1, nums2);
        SortUtils.printArr(option);
    }
}
