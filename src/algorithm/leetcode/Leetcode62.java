package src.algorithm.leetcode;

/**
 * 一个机器人位于一个m x n网格的左上角 （起始点在下图中标记为 “Start” ）。
 * 机器人每次只能向下或者向右移动一步。机器人试图达到网格的右下角（在下图中标记为 “Finish” ）。
 * 问总共有多少条不同的路径？
 *
 * 输入：m = 3, n = 7
 * 输出：28
 *
 * 62. 不同路径
 * @author caoyang
 */
public class Leetcode62 {
    public static int uniquePaths(int m, int n) {
        int[][] dp = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if(i == 0 || j == 0){
                    dp[i][j] = 1;
                } else {
                    dp[i][j] = dp[i-1][j] + dp[i][j-1];
                }

            }
        }
        return dp[m-1][n-1];
    }

    public static void main(String[] args) {
        int m = 3;
        int n = 7;
        int result = uniquePaths(m, n);
        System.out.println(result);
    }
}
