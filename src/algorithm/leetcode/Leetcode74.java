package src.algorithm.leetcode;

/**
 * 74. 搜索二维矩阵
 * 编写一个高效的算法来判断m x n矩阵中，是否存在一个目标值。该矩阵具有如下特性：
 *
 * 每行中的整数从左到右按升序排列。
 * 每行的第一个整数大于前一行的最后一个整数
 *
 * @author caoyang
 */
public class Leetcode74 {
    public static boolean searchMatrix(int[][] matrix, int target) {
        int m = matrix.length;
        int n = matrix[0].length;
        if (target < matrix[0][0] || target > matrix[m-1][n-1]){
            return false;
        }
        int row = 1;
        for (int i = 1; i < m; i++) {
            if (matrix[i][0] > target){
                row = i;
                break;
            } else if (matrix[i][0] == target) {
                return true;
            }
            if(i == m-1){
                row = m;
            }
        }
        return find(matrix[row-1], target, 0, n-1);
    }
    public static boolean find(int[] row, int target, int start, int end){
        if(row[end] < target || row[start] > target){
            return false;
        }
        if(start >= end){
            return row[start] == target;
        }
        int middle = start + ((end - start) >> 1);
        boolean left = find(row, target, start, middle);
        boolean right = find(row, target, middle+1, end);
        return left || right;
    }

    public static void main(String[] args) {
        int[][] matrix = {{1,3,5,7},{10,11,16,20},{23,30,34,50}};
        int target = 30;
        boolean result = searchMatrix(matrix, target);
        System.out.println(result);
    }
}
