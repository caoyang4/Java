package src.algorithm.leetcode;

/**
 * 4. 寻找两个正序数组的中位数
 * 凡是有序子数组，可以用归并思想
 *
 * 给定两个大小分别为 m 和 n 的正序（从小到大）数组nums1 和nums2。请你找出并返回这两个正序数组的中位数 。
 * 算法的时间复杂度应该为 O(log (m+n))
 * @author caoyang
 */
public class Leetcode4 {

    public static double findMedianSortedArrays(int[] nums1, int[] nums2) {
        double res = 0;
        int totalLength = nums1.length + nums2.length;
        boolean isEven = totalLength % 2 == 0;
        int mid = totalLength >> 1;
        int[] mergeArray = new int[totalLength];
        int i = 0;
        int j = 0;
        int mergeIndex = 0;
        while ( i < nums1.length && j< nums2.length){
            mergeArray[mergeIndex] = nums1[i] <= nums2[j] ? nums1[i] : nums2[j];
            if (mid == mergeIndex){
                return isEven ? ((double) mergeArray[mid-1] + (double) mergeArray[mid]) / 2 : mergeArray[mid];
            }
            mergeIndex++;
            if (nums1[i] <= nums2[j]) { i++; } else { j++; }
        }

        while (i < nums1.length){
            mergeArray[mergeIndex] = nums1[i];
            if (mid == mergeIndex){
                return isEven ? ((double) mergeArray[mid-1] + (double) mergeArray[mid]) / 2 : mergeArray[mid];
            }
            i++;
            mergeIndex++;
        }
        while (j < nums2.length){
            mergeArray[mergeIndex] = nums2[j];
            if (mid == mergeIndex){
                return isEven ? ((double) mergeArray[mid-1] + (double) mergeArray[mid]) / 2 : mergeArray[mid];
            }
            j++;
            mergeIndex++;

        }
        return res;
    }
    public static void main(String[] args) {
        int[] nums2 = new int[0];
        int[] nums1 = {2};
        System.out.println(findMedianSortedArrays(nums1, nums2));

    }
}
