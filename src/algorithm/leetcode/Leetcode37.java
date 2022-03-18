package src.algorithm.leetcode;

import java.util.*;

/**
 * 37. 解数独
 * 编写一个程序，通过填充空格来解决数独问题。
 * 数独的解法需 遵循如下规则：
 * 数字 1-9 在每一行只能出现一次。
 * 数字 1-9 在每一列只能出现一次。
 * 数字 1-9 在每一个以粗实线分隔的 3x3 宫内只能出现一次
 * 数独部分空格内已填入了数字，空白格用'.'表示
 *
 * @author caoyang
 */
public class Leetcode37 {
    public void solveSudoku(char[][] board) {
        char[] digits = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};
        trackBack(board, digits);
    }

    public boolean trackBack(char[][] board, char[] digits){
        int len = board.length;
        // 遍历行
        for (int i = 0; i < len; i++) {
            // 遍历列
            for (int j = 0; j < len; j++) {
                if(board[i][j] != '.'){
                    continue;
                }
                for (char digit : digits) {
                    // digit放入board[i][j]是否合适
                    if(! isUsed(board, i, j, digit)){
                        board[i][j] = digit;
                        // 如果找到合适一组立刻返回
                        if(trackBack(board, digits)){
                            return true;
                        }
                        // 回溯，撤销board[i][j]的digit
                        board[i][j] = '.';
                    }
                }
                // 9个数全部试完，都不满足，则返回false
                return false;
            }
        }
        // 遍历完没有返回false，说明找到合适格子位置
        return true;
    }

    public boolean isUsed(char[][] board, int row, int col, char digit){
        int x = row / 3;
        int y = col / 3;
        // 判断数字在行是否有效
        for (int i = 0; i < board.length; i++) {
            if(board[row][i] == digit){
                return true;
            }
        }
        // 判断数字在列是否有效
        for (int i = 0; i < board.length; i++) {
            if(board[i][col] == digit){
                return true;
            }
        }
        // 判断数字在3X3小方块是否有效
        for (int i = x*3; i < (x+1)*3; i++) {
            for (int j = y*3; j < (y+1)*3; j++) {
                if(board[i][j] == digit){
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {

    }
}
