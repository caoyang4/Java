package src.algorithm.leetcode;

import java.util.*;
/**
 * 212. 单词搜索 II
 * 输入：board = [["o","a","a","n"],["e","t","a","e"],["i","h","k","r"],["i","f","l","v"]], words = ["oath","pea","eat","rain"]
 * 输出：["eat","oath"]
 *
 * @author caoyang
 */
public class Leetcode212 {
    public List<String> findWords(char[][] board, String[] words) {
        String[] reverseWords = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            StringBuilder s = new StringBuilder();
            for (int j = words[i].length()-1; j >= 0; j--) {
                s.append(words[i].charAt(j));
            }
            reverseWords[i] = s.toString();
        }
        List<String> result = new ArrayList<>();
        int m = board.length;
        int n = board[0].length;
        boolean[][] used = new boolean[m][n];
        trackBack(board, used, words, reverseWords, result, new LinkedList<>(), 0, 0);
        return result;
    }
    public void trackBack(char[][] board, boolean[][] used, String[] words, String[] reverseWords, List<String> result, Deque<String> path, int row, int col) {
        if (row < 0 || row >= board.length || col < 0 || col >= board[0].length){ return; }
        if (!used[row][col]){
            used[row][col] = true;
            path.add(board[row][col]+"");
            addWord(words, reverseWords, String.join("", path), result);
            if(result.size() == words.length){return;}
            trackBack(board, used, words, reverseWords, result, path, row-1, col);
            trackBack(board, used, words, reverseWords, result, path, row+1, col);
            trackBack(board, used, words, reverseWords, result, path, row, col-1);
            trackBack(board, used, words, reverseWords, result, path, row, col+1);
            path.removeLast();
            used[row][col] = false;
        }
    }
    public void addWord(String[] words, String[] reverseWords, String path, List<String> result){
        for (int i = 0; i < words.length; i++) {
            if ((path.contains(words[i]) || path.contains(reverseWords[i])) && !result.contains(words[i])){
                result.add(words[i]);
            }
        }
    }


    public static void main(String[] args) {

    }
}
