package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 498. 对角线遍历
 * 输入：mat = [[1,2,3],[4,5,6],[7,8,9]]
 * 输出：[1,2,4,7,5,3,6,8,9]
 * @author caoyang
 */
public class Leetcode498 {
    public static int[] findDiagonalOrder(int[][] mat) {
        int m = mat.length;
        int n = mat[0].length;
        int[] result = new int[m * n];
        int index = 0;
        for (int i = 0; i < m + n - 1; i++) {
            List<Integer> list = new ArrayList();
            int row = i < n ? 0 : i - n + 1;
            int col = i < n ? i : n - 1;
            while (row < m && col >= 0){
                list.add(mat[row++][col--]);
            }
            if ((i & 1) == 0){Collections.reverse(list);}
            for (int num : list) {
                result[index++] = num;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[][] mat = {{1,2,3},{4,5,6},{7,8,9}};
        int[] result = findDiagonalOrder(mat);
        System.out.println(Arrays.toString(result));
    }
}
