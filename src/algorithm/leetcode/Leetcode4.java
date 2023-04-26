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
            mergeArray[mergeIndex] = Math.min(nums1[i], nums2[j]);
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
    public static double findMedianSortedArraysBinary(int[] A, int[] B) {
        int m = A.length;
        int n = B.length;
        if (m > n) {
            return findMedianSortedArrays(B,A); // 保证 m <= n
        }
        int iMin = 0, iMax = m;
        while (iMin <= iMax) {
            int i = (iMin + iMax) / 2;
            int j = (m + n + 1) / 2 - i;
            if (j != 0 && i != m && B[j-1] > A[i]){ // i 需要增大
                iMin = i + 1;
            }
            else if (i != 0 && j != n && A[i-1] > B[j]) { // i 需要减小
                iMax = i - 1;
            }
            else { // 达到要求，并且将边界条件列出来单独考虑
                int maxLeft = 0;
                if (i == 0) { maxLeft = B[j-1]; }
                else if (j == 0) { maxLeft = A[i-1]; }
                else { maxLeft = Math.max(A[i-1], B[j-1]); }
                if ( (m + n) % 2 == 1 ) { return maxLeft; } // 奇数的话不需要考虑右半部分

                int minRight = 0;
                if (i == m) { minRight = B[j]; }
                else if (j == n) { minRight = A[i]; }
                else { minRight = Math.min(B[j], A[i]); }

                return (maxLeft + minRight) / 2.0; //如果是偶数的话返回结果
            }
        }
        return 0.0;

    }
    public static void main(String[] args) {
        int[] nums1 = {1,3};
        int[] nums2 = {2,7};
        System.out.println(findMedianSortedArraysBinary(nums1, nums2));

    }
}
