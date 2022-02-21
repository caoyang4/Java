package src.algorithm.leetcode;

/**
 * 一个机器人位于一个m x n网格的左上角 （起始点在下图中标记为 “Start” ）。
 * 机器人每次只能向下或者向右移动一步。机器人试图达到网格的右下角（在下图中标记为 “Finish”）。
 * 现在考虑网格中有障碍物。那么从左上角到右下角将会有多少条不同的路径？
 * 网格中的障碍物和空位置分别用 1 和 0 来表示
 *
 * 输入：obstacleGrid = [[0,0,0],[0,1,0],[0,0,0]]
 * 输出：2
 * 解释：3x3 网格的正中间有一个障碍物。
 *
 * @author caoyang
 */
public class Leetcode63 {
    public static int uniquePathsWithObstacles(int[][] obstacleGrid) {
        int m = obstacleGrid.length;
        if (m == 0){
            return 0;
        }
        int n = obstacleGrid[0].length;
        if (n == 0){
            return 0;
        }
        int[][] f = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if(obstacleGrid[i][j] == 1){
                    f[i][j] = 0;
                } else {
                    if(i == 0 && j == 0){
                        f[i][j] = 1;
                    } else {
                        f[i][j] = 0;
                        if (j >= 1){
                            f[i][j] += f[i][j-1];
                        }
                        if (i >= 1){
                            f[i][j] += f[i-1][j];
                        }
                    }
                }

            }
        }
        return f[m-1][n-1];
    }

    public static void main(String[] args) {
        int[][] obstacleGrid = {{1,0}};
        int res = uniquePathsWithObstacles(obstacleGrid);
        System.out.println(res);
    }

}
