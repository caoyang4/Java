package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 52. N皇后 II
 *
 * n皇后问题 研究的是如何将 n个皇后放置在 n × n 的棋盘上，并且使皇后彼此之间不能相互攻击。
 * 给你一个整数 n ，返回 n 皇后问题不同的解决方案的数量
 *
 * @author caoyang
 */
public class Leetcode52 {
    public static int totalNQueens(int n) {
        int[] result = new int[1];
        char[][] chess = new char[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(chess[i], '.');
        }
        trackBack(result, chess, 0);
        return result[0];
    }

    public static void trackBack(int[] result, char[][] chess, int start){
        int len = chess.length;
        if (start == len){
            result[0]++;
            return;
        }
        for (int j = 0; j < len; j++) {
            if (!isValid(chess, start, j)){
                continue;
            }
            chess[start][j] = 'Q';
            trackBack(result, chess, start+1);
            chess[start][j] = '.';
        }
    }

    public static boolean isValid(char[][] chess, int row, int col){
        int len = chess.length;
        for (int i = 0; i < len; i++) {
            if (i != row && chess[i][col] == 'Q'){
                return false;
            }
            if (i != col && chess[row][i] == 'Q'){
                return false;
            }
        }
        int i = row;
        int j = col;
        while (i > 0 && j > 0){
            if (chess[--i][--j] == 'Q'){
                return false;
            }
        }
        i = row;
        j = col;
        while (i > 0 && j < len-1){
            if (chess[--i][++j] == 'Q'){
                return false;
            }
        }
        i = row;
        j = col;
        while (i < len-1 && j > 0){
            if (chess[++i][--j] == 'Q'){
                return false;
            }
        }
        i = row;
        j = col;
        while (i < len-1 && j < len-1){
            if (chess[++i][++j] == 'Q'){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        int n = 4;
        int result = totalNQueens(n);
        System.out.println(result);
    }
}
