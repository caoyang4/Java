package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 378. 有序矩阵中第K小的元素
 * 它是排序后的第 k 小元素，而不是第k个不同的元素
 *
 * 输入：matrix = [[1,5,9],[10,11,13],[12,13,15]], k = 8
 * 输出：13
 * @author caoyang
 */
public class Leetcode378 {
    public static int kthSmallest(int[][] matrix, int k) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[] nums = new int[m * n];
        int index = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                nums[index++] = matrix[i][j];
            }
        }
        Arrays.sort(nums);
        return nums[k-1];
    }

    public static void main(String[] args) {
        int[][] matrix = {{1,2},{1,3}};
        int k = 4;
        int result = kthSmallest(matrix, k);
        System.out.println(result);
    }
}
