package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 130. 被围绕的区域
 * 给你一个 m x n 的矩阵 board ，由若干字符 'X' 和 'O' ，找到所有被 'X' 围绕的区域，并将这些区域里所有的 'O' 用 'X' 填充
 *
 * 输入：board = [["X","X","X","X"],["X","O","O","X"],["X","X","O","X"],["X","O","X","X"]]
 * 输出：[["X","X","X","X"],["X","X","X","X"],["X","X","X","X"],["X","O","X","X"]]
 *
 * @author caoyang
 */
public class Leetcode130 {
    public static void solve(char[][] board) {
        int m = board.length;
        int n = board[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                boolean isEdge = i==0 || j==0 || i==m-1 || j==n-1;
                // 从边缘O开始搜索
                if (isEdge && board[i][j] == 'O'){
                    trackBack(board, m, n, i, j);
                }
            }
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if(board[i][j] == 'O'){
                    board[i][j] = 'X';
                } else if(board[i][j] == '#'){
                    board[i][j] = 'O';
                }
            }
        }

    }
    public static void trackBack(char[][] board, int m, int n, int row, int col){
        if (row < 0 || row >= m || col < 0 || col >= n || board[row][col] == 'X' || board[row][col] == '#'){
            return;
        }
        board[row][col] = '#';
        trackBack(board, m, n, row ,col-1);
        trackBack(board, m, n, row ,col+1);
        trackBack(board, m, n, row-1 ,col);
        trackBack(board, m, n, row+1 ,col);
    }
    public static void main(String[] args) {
        char[][] board = {{'O','X','X','O','X'},{'X','O','O','X','O'},
                {'X','O','X','O','X'},{'O','X','O','O','O'},{'X','X','O','X','O'}};
        for (char[] chars : board) {
            System.out.println(Arrays.toString(chars));
        }
        System.out.println();
        solve(board);
        for (char[] chars : board) {
            System.out.println(Arrays.toString(chars));
        }
    }
}
