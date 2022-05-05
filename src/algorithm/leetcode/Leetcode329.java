package src.algorithm.leetcode;

/**
 * 329. 矩阵中的最长递增路径
 * 给定一个 m x n 整数矩阵 matrix ，找出其中最长递增路径的长度
 *
 * 输入：matrix = [[9,9,4],[6,6,8],[2,1,1]]
 * 输出：4
 * @author caoyang
 */
public class Leetcode329 {
    public static int longestIncreasingPath(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[][] maxLength = new int[m][n];
        int max = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                maxLength[i][j] = trackBack(matrix, maxLength, i, j);
                max = Math.max(maxLength[i][j], max);
            }
        }
        return max;
    }
    public static int trackBack(int[][] matrix, int[][] maxLength, int row, int col){
        int m = matrix.length;
        int n = matrix[0].length;
        if (row < 0 || row >= m || col < 0 || col >= n){return 0;}
        if (maxLength[row][col] != 0){return maxLength[row][col];}
        int left = col > 0 && matrix[row][col-1] > matrix[row][col] ? trackBack(matrix, maxLength, row, col-1) : 0;
        int right = col < n-1 && matrix[row][col+1] > matrix[row][col] ? trackBack(matrix,maxLength, row, col+1) : 0;
        int down = row > 0 && matrix[row-1][col] > matrix[row][col] ? trackBack(matrix, maxLength, row-1, col) : 0;
        int up = row < m-1 && matrix[row+1][col] > matrix[row][col] ? trackBack(matrix, maxLength, row+1, col) : 0;
        return maxLength[row][col] = Math.max(left, Math.max(right, Math.max(down, up)))+1;
    }

    public static void main(String[] args) {
        int[][] matrix = {{9,9,4},{6,6,8},{2,1,1}};
        int result = longestIncreasingPath(matrix);
        System.out.println(result);
    }

}
