package src.algorithm.leetcode;

/**
 * 695. 岛屿的最大面积
 * 给你一个大小为 m x n 的二进制矩阵 grid 。
 * 岛屿是由一些相邻的 1 (代表土地) 构成的组合，这里的「相邻」要求两个 1 必须在 水平或者竖直的四个方向上相邻。
 * 你可以假设 grid 的四个边缘都被 0（代表水）包围着。
 * 岛屿的面积是岛上值为 1 的单元格的数目。
 * 计算并返回 grid 中最大的岛屿面积。如果没有岛屿，则返回面积为 0
 *
 * 输入：grid = [[0,0,1,0,0,0,0,1,0,0,0,0,0],
 *              [0,0,0,0,0,0,0,1,1,1,0,0,0],
 *              [0,1,1,0,1,0,0,0,0,0,0,0,0],
 *              [0,1,0,0,1,1,0,0,1,0,1,0,0],
 *              [0,1,0,0,1,1,0,0,1,1,1,0,0],
 *              [0,0,0,0,0,0,0,0,0,0,1,0,0],
 *              [0,0,0,0,0,0,0,1,1,1,0,0,0],
 *              [0,0,0,0,0,0,0,1,1,0,0,0,0]]
 * 输出：6
 *
 * @author caoyang
 */
public class Leetcode695 {
    public static int maxAreaOfIsland(int[][] grid) {
        int maxArea = 0;
        int m = grid.length;
        int n = grid[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if(grid[i][j] != 1) continue;
                maxArea = Math.max(maxArea, trackBack(grid, m, n, i, j));
            }
        }
        return maxArea;
    }
    public static int trackBack(int[][] grid, int m, int n, int row, int col){
        if (row < 0 || row >= m || col < 0 || col >= n || grid[row][col] != 1) return 0;
        grid[row][col] = 0;
        return trackBack(grid, m, n, row-1, col)
                + trackBack(grid, m, n, row+1, col)
                + trackBack(grid, m, n, row, col-1)
                + trackBack(grid, m, n, row, col+1) + 1;
    }

    public static void main(String[] args) {
        int[][] grid = {{0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
                        {0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0},
                        {0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0}
        };
        int result = maxAreaOfIsland(grid);
        System.out.println(result);
    }
}
