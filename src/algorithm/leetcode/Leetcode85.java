package src.algorithm.leetcode;

import java.util.ArrayDeque;
import java.util.Stack;

/**
 * 85. 最大矩形
 * 给定一个仅包含 0 和 1 、大小为 rows x cols 的二维二进制矩阵，找出只包含 1 的最大矩形，并返回其面积
 *
 * 输入：matrix = [["1","0","1","0","0"],["1","0","1","1","1"],["1","1","1","1","1"],["1","0","0","1","0"]]
 * 输出：6
 *
 * @author caoyang
 */
public class Leetcode85 {
    public static int maximalRectangle(char[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[] heights = new int[n];

        for (int i = 0; i < n; i++) {
            if(matrix[0][i] == '1') {
                heights[i] = 1;
            }
        }
        int result = maxTangleArea(heights);
        for (int i = 1; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if(matrix[i][j] == '1'){
                    heights[j] += 1;
                } else {
                    heights[j] = 0;
                }
            }
            result = Math.max(result, maxTangleAreaStack(heights));
        }
        return result;
    }

    public static int maxTangleAreaStack(int[] heights){
        int[] tmp = new int[heights.length+2];
        System.arraycopy(heights, 0, tmp, 1, heights.length);
        Stack<Integer> stack = new Stack<>();
        int area = 0;
        for (int i = 0; i < tmp.length; i++) {
            while (!stack.isEmpty() && tmp[i] < tmp[stack.peek()]){
                int h = tmp[stack.pop()];
                area = Math.max(area, (i - stack.peek() -1) * h);
            }
            stack.push(i);
        }
        return area;
    }

    public static int maxTangleArea(int[] heights){
        int len = heights.length;
        int[] minLeft = new int[len];
        int[] minRight = new int[len];
        minLeft[0] = -1;
        minRight[len-1] = len;
        for (int i = 1; i < len; i++) {
            int t = i - 1;
            while (t >= 0 && heights[t] >= heights[i]){
                t = minLeft[t];
            }
            minLeft[i] = t;
        }
        for (int i = len-2; i >= 0; i--) {
            int t = i + 1;
            while (t < len && heights[t] >= heights[i]){
                t = minRight[t];
            }
            minRight[i] = t;
        }

        int maxArea = 0;
        for (int i = 0; i < len; i++) {
            maxArea = Math.max(maxArea, heights[i]*(minRight[i] - minLeft[i] - 1));
        }
        return maxArea;
    }

    public static void main(String[] args) {
        char[][] matrix = {{'1','0','1','0','0'},{'1','0','1','1','1'},{'1','1','1','1','1'},{'1','0','0','1','0'}};
        int result = maximalRectangle(matrix);
        System.out.println(result);
    }
}
