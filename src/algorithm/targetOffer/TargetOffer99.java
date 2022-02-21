package src.algorithm.targetOffer;

/**
 * 给定一个包含非负整数的 m x n 网格grid ，请找出一条从左上角到右下角的路径，使得路径上的数字总和为最小。
 * 说明：一个机器人每次只能向下或者向右移动一步。
 *
 * 输入：grid = [[1,3,1],[1,5,1],[4,2,1]]
 * 输出：7
 * 解释：因为路径 1→3→1→1→1 的总和最小
 *
 * @author caoyang
 */
public class TargetOffer99 {
    public static int minPathSum(int[][] grid) {
        int m = grid.length;
        if (m == 0){ return 0; }
        int n = grid[0].length;
        if (n == 0){ return 0; }
        int[][] sum = new int[m][n];
        sum[0][0] = grid[0][0];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if(i==0 && j==0){ continue; }
                if(i == 0){
                    sum[i][j] = sum[i][j-1] + grid[i][j];
                } else if (j == 0){
                    sum[i][j] = sum[i-1][j] + grid[i][j];
                } else {
                    sum[i][j] = Math.min(sum[i][j-1], sum[i-1][j]) + grid[i][j];
                }
            }
        }
        return sum[m-1][n-1];
    }

    public static void main(String[] args) {
        int[][] grid = {{1,3,1},{1,5,1},{4,2,1}};
        int res = minPathSum(grid);
        System.out.println(res);
    }
}
