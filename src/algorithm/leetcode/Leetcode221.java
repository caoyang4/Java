package src.algorithm.leetcode;

/**
 * 221. 最大正方形
 * 在一个由 '0' 和 '1' 组成的二维矩阵内，找到只包含 '1' 的最大正方形，并返回其面积
 * 输入：matrix = [["1","0","1","0","0"],["1","0","1","1","1"],["1","1","1","1","1"],["1","0","0","1","0"]]
 * 输出：4
 *
 * @author caoyang
 */
public class Leetcode221 {
    public static int maximalSquare(char[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int[] heights = new int[n];
        for (int i = 0; i < n; i++) {
            heights[i] = matrix[0][i] == '1' ? 1: 0;
        }
        int max = subLargestSquare(heights, n);
        for (int i = 1; i < m; i++) {
            for (int j = 0; j < n; j++) {
                heights[j] = matrix[i][j] == '1' ? heights[j]+1 : 0;
            }
            max = Math.max(max, subLargestSquare(heights, n));
        }
        return max;
    }
    public static int subLargestSquare(int[] heights, int n){
        int[] leftGreat = new int[n];
        int[] rightGreat = new int[n];

        leftGreat[0] = -1;
        for (int i = 1; i < n; i++) {
            int t = i-1;
            while (t >= 0 && heights[t] >= heights[i]){
                t = leftGreat[t];
            }
            leftGreat[i] = t;
        }
        rightGreat[n-1] = n;
        for (int i = n-2; i >= 0; i--) {
            int t = i+1;
            while (t < n && heights[t] >= heights[i]){
                t = rightGreat[t];
            }
            rightGreat[i] = t;
        }
        int max = 0;
        for (int i = 0; i < n; i++) {
            if(heights[i] == 0){continue;}
            int side = Math.min(heights[i], rightGreat[i]-leftGreat[i]-1);
            max = Math.max(max, side*side);
        }
        return max;
    }

    public static void main(String[] args) {
        char[][] matrix = {{'1','0','1','0','0'},{'1','0','1','1','1'},{'1','1','1','1','1'},{'1','0','0','1','0'}};
        int reuslt = maximalSquare(matrix);
        System.out.println(reuslt);
    }
}
