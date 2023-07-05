package src.algorithm;

import src.algorithm.leetcode.TreeNode;

import java.util.*;

/**
 * @author caoyang
 * @create 2023-04-14 10:57
 */
public class Solution {
    public int maxArea(int[] height) {
        int[] newHeight = new int[height.length + 1];
        System.arraycopy(height,0,newHeight,1, height.length);
        Stack<Integer> stack = new Stack<>();
        int area = 0;
        for (int i = 0; i < newHeight.length; i++) {
            while (!stack.isEmpty() && newHeight[i] < newHeight[stack.peek()]){
                int h = newHeight[stack.pop()];
                area = Math.max(area, h*(i-stack.peek()-1));
            }
            stack.push(i);
        }
        return area;
    }
    public boolean isMatch(String s, String p) {
        int m = s.length();
        int n = p.length();
        boolean[][]  match = new boolean[m+1][n+1];
        char[] sChars = s.toCharArray();
        char[] pChars = p.toCharArray();
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 && j == 0){
                    match[i][j] = true;
                    continue;
                }
                if (j == 0){
                    match[i][j] = false;
                    continue;
                }
                if (pChars[j-1] != '*') {
                    if (i > 0 && (sChars[i-1] == pChars[j-1] || pChars[j-1] == '.')){
                        match[i][j] = match[i-1][j-1];
                    } else {
                        if (j > 1){
                            // *表示 0 个字符
                            match[i][j] = match[i][j-2];
                            if (i > 0 && (sChars[i-1] == pChars[j-2] || pChars[j-2] == '.')){
                                // * 表示多个字符
                                match[i][j] |= match[i-1][j];
                            }
                        }
                    }
                }
            }
        }
        return match[m][n];
    }

    public List<String> generateParenthesis(int n) {
        List<List<String>> result = new ArrayList<>(Arrays.asList(Collections.singletonList(""), Collections.singletonList("()")));
        for (int i = 2; i <= n; i++) {
            List<String> tmp = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                List<String> pList = result.get(j);
                List<String> qList = result.get(i-j-1);
                pList.forEach(p -> qList.forEach(q -> {
                    String r = "(" + p + ")" + q;
                    tmp.add(r);
                }));
            }
            result.add(tmp);
        }
        return result.get(n);
    }

    public void solveSudoku(char[][] board) {
        char[] digits = {'1','2','3','4','5','6','7','8','9'};
        trackBack(board, digits);
    }

    public boolean trackBack(char[][] board, char[] digits){
        int m = board.length;
        int n = board[0].length;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] != '.') {
                    continue;
                }
                for (char digit : digits) {
                    if (!isUsed(board, digit, i, j)){
                        board[i][j] = digit;
                        if (trackBack(board, digits)) {
                            return true;
                        }
                        board[i][j] = '.';
                    }
                }
            }
        }
        return false;
    }
    public boolean isUsed(char[][] board, char digit, int i, int j){
        int x = i / 3;
        int y = j / 3;
        int m = board.length;
        int n = board[0].length;
        for (int k = 0; k < n; k++) {
            if(k != j && board[i][k] == digit){
                return true;
            }
        }
        for (int k = 0; k < m; k++) {
            if(k != i && board[k][j] == digit){
                return true;
            }
        }

        for (int k = 3*x; k < 3*(x+1); k++) {
            for (int l = 3*y; l < 3*(y+1); l++) {
                if(k != i && l != j && board[k][l] == digit){
                    return true;
                }
            }
        }
        return false;
    }

    public static List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] > 0) {
                return result;
            }
            if (i > 0 && nums[i] == nums[i-1]){
                continue;
            }
            int left = i + 1;
            int right = nums.length - 1;
            while (left < right){
                int t = nums[i] + nums[left] + nums[right];
                if (t == 0){
                    result.add(Arrays.asList(nums[i],nums[left],nums[right]));
                    while (left < right && nums[left] == nums[left+1]){
                        left++;
                    }
                    while (left < right && nums[right] == nums[right-1]){
                        right--;
                    }
                    left++;
                    right--;
                } else if (t > 0){
                    right--;
                } else {
                    left++;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {

    }

}
