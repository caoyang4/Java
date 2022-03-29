package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 59. 螺旋矩阵 II
 * 给你一个正整数 n ，生成一个包含 1 到 n^2 所有元素，且元素按顺时针顺序螺旋排列的 n x n 正方形矩阵 matrix
 * @author caoyang
 */
public class Leetcode59 {
    public static int[][] generateMatrix(int n) {
        int[][] result = new int[n][n];
        int index = 1;
        int start = 0;
        while (index <= n*n){
            int i = start;
            int j = start;
            if(index == n*n){
                result[i][j] = index;
                break;
            }
            for (; j < n-start-1; j++) {
                result[i][j] = index++;
            }
            for (; i < n-start-1; i++) {
                result[i][j] = index++;
            }
            for (; j > start; j--) {
                result[i][j] = index++;
            }
            for (; i > start; i--) {
                result[i][j] = index++;
            }
            start++;
        }
        return result;
    }

    public static void main(String[] args) {
        int n = 3;
        int[][] result = generateMatrix(n);
        for (int[] ints : result) {
            System.out.println(Arrays.toString(ints));
        }
    }
}
