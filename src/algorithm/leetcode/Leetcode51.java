package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 51. N 皇后
 * @author caoyang
 */
public class Leetcode51 {
    public static List<List<String>> solveNQueens(int n) {
        List<List<String>> result = new ArrayList<>();
        List<String> pieces = new ArrayList<>();
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < n; i++) {
            dots.append(".");
        }
        for (int i = 0; i < n; i++) {
            pieces.add(dots.toString());
        }
        trackBack(result, pieces, n, 0);
        return result;
    }

    public static void trackBack(List<List<String>> result, List<String> pieces, int n, int start){
        if (start == n){
            result.add(new ArrayList<>(pieces));
            return;
        }
        for (int j = 0; j < n; j++) {
            if(pieces.get(start).charAt(j) == 'Q'){
                // 棋盘 [i,j] 上已有皇后，跳出该列，找下一行
                break;
            }
            if(isValid(pieces, n, start, j)){
                String piece = fillQueens(pieces.get(start), j, "Q");
                pieces.set(start, piece);
                trackBack(result, pieces, n, start+1);
                piece = fillQueens(pieces.get(start), j, ".");
                pieces.set(start, piece);
            }
        }
    }

    public static String fillQueens(String piece, int j, String replacement){
        StringBuilder tmp = new StringBuilder(piece);
        tmp.replace(j, j+1, replacement);
        return tmp.toString();
    }

    /**
     * 不能同行
     * 不能同列
     * 不能同斜线
     * @param pieces
     * @param n
     * @param row
     * @param col
     * @return
     */
    public static boolean isValid(List<String> pieces, int n, int row, int col){
        for (int i = 0; i < n; i++) {
            // 竖列不能有其他皇后
            if (i != row && pieces.get(i).charAt(col) == 'Q'){
                return false;
            }
            // 横排不能有其他皇后
            if (i != col && pieces.get(row).charAt(i) == 'Q'){
                return false;
            }
        }
        // 十字斜线不能有其他皇后
        int i = row, j = col;
        while (i > 0 && j > 0){
            if(pieces.get(--i).charAt(--j) == 'Q'){
                return false;
            }
        }
        i = row;
        j = col;
        while (i < n-1 && j > 0){
            if(pieces.get(++i).charAt(--j) == 'Q'){
                return false;
            }
        }
        i = row;
        j = col;
        while (i > 0 && j < n-1){
            if(pieces.get(--i).charAt(++j) == 'Q'){
                return false;
            }
        }
        i = row;
        j = col;
        while (i < n-1 && j < n-1){
            if(pieces.get(++i).charAt(++j) == 'Q'){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        List<List<String>> result = solveNQueens(4);
        for (List<String> list : result) {
            System.out.println(list);
        }
    }
}
