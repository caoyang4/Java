package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 螺旋矩阵
 * 给你一个 m 行 n 列的矩阵 matrix ，请按照顺时针螺旋顺序 ，返回矩阵中的所有元素
 * @author caoyang
 */
public class Leetcode54 {
    public static List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> result = new ArrayList<>();
        int m = matrix.length;
        int n = matrix[0].length;

        int start = 0;
        while (result.size() < m * n){
            int i = start;
            int j = start;
            for (; j < n - start - 1; j++) {
                result.add(matrix[i][j]);
            }
            // 单行矩阵情况
            if (result.size() == m * n - 1){
                result.add(matrix[i][j]);
                break;
            }
            for (; i < m - start - 1; i++) {
                result.add(matrix[i][j]);
            }
            // 单列矩阵情况
            if (result.size() == m * n - 1){
                result.add(matrix[i][j]);
                break;
            }
            for (; j > start; j--) {
                result.add(matrix[i][j]);
            }
            for (; i > start; i--) {
                result.add(matrix[i][j]);
            }
            start++;
        }
        return result;
    }

    public static void main(String[] args) {
        int[][] matrix = {{1,2,3,4},{5,6,7,8},{9,10,11,12}};
        List<Integer> result = spiralOrder(matrix);
        System.out.println(result);

    }

}
