package src.algorithm.leetcode;

/**
 * 200. 岛屿数量
 * 输入：grid = [
 *   ["1","1","1","1","0"],
 *   ["1","1","0","1","0"],
 *   ["1","1","0","0","0"],
 *   ["0","0","0","0","0"]
 * ]
 * 输出：1
 *
 * @author caoyang
 */
public class Leetcode200 {
    public static int numIslands(char[][] grid) {
        int m = grid.length;
        int n = grid[0].length;
        int count = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                char c = grid[i][j];
                if(c == '1'){
                    trackBack(grid, m, n, i, j);
                    count++;
                }
            }
        }
        return count;
    }
    public static void trackBack(char[][] grid, int m, int n, int row, int col){
        if (row < 0 || row >= m || col < 0 || col >= n || grid[row][col] != '1'){
            return;
        }
        grid[row][col] = '#';
        trackBack(grid, m, n, row-1, col);
        trackBack(grid, m, n, row+1, col);
        trackBack(grid, m, n, row, col-1);
        trackBack(grid, m, n, row, col+1);
    }

    public static void main(String[] args) {
        char[][] grid = {
                {'1','1','0','0','0'},
                {'1','1','0','0','0'},
                {'0','0','1','0','0'},
                {'0','0','0','1','1'}
        };
        int result = numIslands(grid);
        System.out.println(result);
    }
}
