package src.algorithm.leetcode;

/**
 * 79. 单词搜索
 *
 * 给定一个m x n 二维字符网格board 和一个字符串单词word 。如果word存在于网格中，返回true；否则，返回 false 。
 * 单词必须按照字母顺序，通过相邻的单元格内的字母构成，其中“相邻”单元格是那些水平相邻或垂直相邻的单元格。
 * 同一个单元格内的字母不允许被重复使用
 *
 * 输入：board = [["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]], word = "ABCCED"
 * 输出：true
 *
 * @author caoyang
 */
public class Leetcode79 {
    public static boolean exist(char[][] board, String word) {
        int row = board.length;
        int col = board[0].length;
        boolean[][] used = new boolean[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if(dfs(board, word, 0, used, i, j)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean dfs(char[][] board, String word, int size, boolean[][] used, int row, int col){
        if(size == word.length()){
            return true;
        }
        if(row < 0 || row >= board.length || col < 0 || col >= board[0].length || board[row][col] != word.charAt(size)){
            return false;
        }
        if(!used[row][col]){
            used[row][col] = true;
            // 上下左右搜索
            if(dfs(board, word, size+1, used, row+1, col)
                    || dfs(board, word, size+1, used, row-1, col)
                    || dfs(board, word, size+1, used, row, col-1)
                    || dfs(board, word, size+1, used, row, col+1)
            ){
                return true;
            }
            used[row][col] = false;
        }
        return false;
    }

    public static void main(String[] args) {
        char[][] board = {{'A','B','C','E'}, {'S','F','C','S'}, {'A','D','E','E'}};
        String word = "ABCCED";
        boolean result = exist(board, word);
        System.out.println(result);
    }
}
