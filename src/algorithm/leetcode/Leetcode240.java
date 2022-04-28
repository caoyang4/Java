package src.algorithm.leetcode;

/**
 * 240. 搜索二维矩阵 II
 * 搜索mxn矩阵 matrix 中的一个目标值 target 。该矩阵具有以下特性：
 * 每行的元素从左到右升序排列。
 * 每列的元素从上到下升序排列
 *
 * @author caoyang
 */
public class Leetcode240 {
    public static boolean searchMatrix(int[][] matrix, int target) {
        int m = matrix.length;
        int n =matrix[0].length;
        return search(matrix, target, 0, m-1, 0, n-1);
    }

    public static boolean search(int[][] matrix, int target, int rowStart, int rowEnd, int colStart, int colEnd){
        if (rowStart > rowEnd || colStart > colEnd || target < matrix[rowStart][colStart] || target > matrix[rowEnd][colEnd]){
            return false;
        }
        if (rowStart == rowEnd && colStart == colEnd){
            return target == matrix[rowStart][colStart];
        }
        int rowMiddle = rowStart + ((rowEnd-rowStart) >> 1);
        int colMiddle = colStart + ((colEnd-colStart) >> 1);
        int middle = matrix[rowMiddle][colMiddle];
        if (middle == target){ return true; }
        return search(matrix, target, rowStart, rowMiddle, colStart, colMiddle) ||
                search(matrix, target, rowStart, rowMiddle, colMiddle+1, colEnd) ||
                search(matrix, target, rowMiddle+1, rowEnd, colStart, colMiddle) ||
                search(matrix, target, rowMiddle+1, rowEnd, colMiddle+1, colEnd);
    }

    public static void main(String[] args) {
        int[][] matrix = {{1,4,7,11,15},{2,5,8,12,19},{3,6,9,16,22},{10,13,14,17,24},{18,21,23,26,30}};
        int target = 2;
        boolean result = searchMatrix(matrix, target);
        System.out.println(result);

    }
}
