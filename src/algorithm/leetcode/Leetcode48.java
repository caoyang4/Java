package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 48. 旋转图像
 *
 * 给定一个 n×n 的二维矩阵matrix表示一个图像。请你将图像顺时针旋转 90 度
 * 你必须在原地旋转图像，这意味着你需要直接修改输入的二维矩阵
 * 请不要使用另一个矩阵来旋转图像
 *
 * @author caoyang
 */
public class Leetcode48 {
    public static void rotate(int[][] matrix) {
        int len =  matrix.length;
        Deque<Integer> deque = new LinkedList<>();
        int start = 0;
        while (deque.size() < len * len) {
            int i = start;
            int j = start;
            // 奇数维矩阵情况
            if ((len-1) / 2 == start && (len-1) % 2 == 0){
                deque.add(matrix[i][j]);
            }
            for (; j < len-start-1; j++) {
                deque.add(matrix[i][j]);
            }
            for (; i < len-start-1; i++) {
                deque.add(matrix[i][j]);
            }
            for (; j > start; j--) {
                deque.add(matrix[i][j]);
            }
            for (; i > start; i--) {
                deque.add(matrix[i][j]);
            }
            start++;
        }

        start = 0;
        while (!deque.isEmpty()){
            int i = start;
            int j = len - 1 - start;
            if ((len-1) / 2==start && (len-1) % 2 == 0){
                matrix[i][j] = deque.pop();
            }
            for (;  i < len-start-1; i++) {
                matrix[i][j] = deque.pop();
            }

            for (; j > start; j--) {
                matrix[i][j] = deque.pop();
            }
            for (; i > start; i--) {
                matrix[i][j] = deque.pop();
            }
            for (; j < len-start-1; j++) {
                matrix[i][j] = deque.pop();
            }
            start++;
        }
    }

    public static void main(String[] args) {
        int[][] matrix = {{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,16}};
//        int[][] matrix = {{1,2,3},{4,5,6},{7,8,9}};
//        int[][] matrix = {{1,2},{3,4}};
        System.out.println("原数组：");
        for (int[] ints : matrix) {
            System.out.println(Arrays.toString(ints));
        }
        rotate(matrix);
        System.out.println("顺时针旋转90度后：");
        for (int[] ints : matrix) {
            System.out.println(Arrays.toString(ints));
        }
    }
}
