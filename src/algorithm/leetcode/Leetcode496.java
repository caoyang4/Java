package src.algorithm.leetcode;

import java.util.*;

/**
 * 496. 下一个更大元素 I
 * 
 * 输入：nums1 = [2,4], nums2 = [1,2,3,4]
 * 输出：[3,-1]
 * @author caoyang
 * @create 2022-05-24 10:46
 */
public class Leetcode496 {
    public static int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Map<Integer, Integer> map = new HashMap<>();
        int n = nums2.length;
        for (int i = 0; i < n; i++) map.put(nums2[i], i);
        int m = nums1.length;
        int[] result = new int[m];
        Arrays.fill(result, -1);
        for (int i = 0; i < m; i++) {
            int start = map.get(nums1[i]);
            for (int j = start+1; j < n; j++) {
                if (nums2[j] > nums1[i]){
                    result[i] = nums2[j];
                    break;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums1 = {2,4};
        int[] nums2 = {1,2,3,4};
        int[] result = nextGreaterElement(nums1, nums2);
        System.out.println(Arrays.toString(result));
    }
}
