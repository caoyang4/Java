package src.algorithm.leetcode;

/**
 * 547. 省份数量
 * 输入：isConnected = [[1,1,0],[1,1,0],[0,0,1]]
 * 输出：2
 * @author caoyang
 */
public class Leetcode547 {
    public static int findCircleNum(int[][] isConnected) {
        int n = isConnected.length;
        boolean[] used = new boolean[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (!used[i]){
                trackBack(isConnected, used, n, i);
                count++;
            }
        }
        return count;
    }
    public static void trackBack(int[][] isConnected, boolean[] used, int n, int row){
        for (int j = 0; j < n; j++) {
            if (!used[j] && isConnected[row][j] == 1){
                used[j] = true;
                trackBack(isConnected, used, n, j);
            }
        }
    }

    public static void main(String[] args) {
        int[][] isConnected = {{1,1,0}, {1,1,0}, {0,0,1}};
        int result = findCircleNum(isConnected);
        System.out.println(result);
    }
}
