package src.algorithm.leetcode;

/**
 * 36. 有效的数独
 *
 * 数字 1-9 在每一行只能出现一次。
 * 数字 1-9 在每一列只能出现一次。
 * 数字 1-9 在每一个以粗实线分隔的 3x3 宫内只能出现一次
 *
 * @author caoyang
 */
public class Leetcode36 {
    public static boolean isValidSudoku(char[][] board) {
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++){
                if(board[i][j] != '.'){
                    if (!isValid(board, i, j)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isValid(char[][] board, int row, int col){
        for(int i=0; i<9; i++){
            if(board[i][col] == board[row][col] && row != i){
                return false;
            }
            if(board[row][i] == board[row][col] && col != i){
                return false;
            }
        }

        int m = row / 3;
        int n = col / 3;
        for(int i=m*3; i<(m+1)*3; i++){
            for(int j=n*3; j<(n+1)*3; j++){
                if(i != row && j != col && board[i][j] == board[row][col]){
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {

    }
}
