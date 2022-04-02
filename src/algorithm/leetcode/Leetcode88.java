package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 88. 合并两个有序数组
 * 给你两个按 非递减顺序 排列的整数数组nums1 和 nums2，另有两个整数 m 和 n ，分别表示 nums1 和 nums2 中的元素数目。
 * 请你 合并 nums2 到 nums1 中，使合并后的数组同样按 非递减顺序 排列。
 * 注意：最终，合并后数组不应由函数返回，而是存储在数组 nums1 中。nums1 的初始长度为 m + n
 *
 * 输入：nums1 = [1,2,3,0,0,0], m = 3, nums2 = [2,5,6], n = 3
 * 输出：[1,2,2,3,5,6]
 *
 * @author caoyang
 */
public class Leetcode88 {
    public static void merge(int[] nums1, int m, int[] nums2, int n) {
        int p = m-- + n-- - 1;
        while (m >= 0 && n >= 0) {
            nums1[p--] = nums1[m] > nums2[n] ? nums1[m--] : nums2[n--];
        }

        while (n >= 0) {
            nums1[p--] = nums2[n--];
        }
    }

    public static void merge1(int[] nums1, int m, int[] nums2, int n) {
        if (n == 0){
            return;
        }

        int[] r = new int[m+n];
        int i = 0, j = 0;
        int k = 0;
        while (k < m + n){
            if(i < m && j < n){
                r[k++] = nums1[i] <= nums2[j] ? nums1[i++] : nums2[j++];
            } else {
                if(i < m){
                    r[k++] = nums1[i++];
                } else if (j < n) {
                    r[k++] = nums2[j++];
                }
            }
        }
        for (i = 0; i < m+n; i++) {
            nums1[i] = r[i];
        }

    }


    public static void main(String[] args) {
        int[] nums1 = {1,2,3,0,0,0};
        int m = 3;
        int[] nums2 = {2,5,6};
        int n = 3;
        merge(nums1, m, nums2, n);
        System.out.println(Arrays.toString(nums1));
    }
}
