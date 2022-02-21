package src.algorithm.targetOffer;

/**
 * 一个机器人位于一个 m x n 网格的左上角 （起始点在下图中标记为 “Start” ）。
 * 机器人每次只能 向下 或者 向右 移动一步。机器人试图达到网格的右下角（在下图中标记为 “Finish” ）。
 * 问总共有多少条不同的路径？
 *
 * 输入：m = 3, n = 7
 * 输出：28
 *
 * 动态规划问题
 * @author caoyang
 */
public class TargetOffer98 {
    public static int uniquePaths(int m, int n) {
        int[][] f = new int[m][n];
        // 初始位置即不动，一条路径
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if(i == 0 || j == 0){
                    f[i][j] = 1;
                } else {
                    f[i][j] = f[i-1][j] + f[i][j-1];
                }
            }
        }
        return f[m-1][n-1];
    }

    public static void main(String[] args) {
        int m = 3;
        int n = 7;
        int num = uniquePaths(m, n);
        System.out.println(num);
    }
}
