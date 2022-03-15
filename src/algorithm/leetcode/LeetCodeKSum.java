package src.algorithm.leetcode;

/**
 * K数之和
 * @author caoyang
 */
public class LeetCodeKSum {
    public static int kSum(int[] arr, int n, int target){
        if (arr == null){ return 0; }
        int l = arr.length;
        // dp[i][j][k] 表示前i个数中有j个数的和为k的个数
        // dp[i][j][k] = dp[i-1][j][k] + dp[i-1][j-1][k-arr[i-1]
        // int[][][] dp = new int[l+1][n+1][target+1];
        // 滚动数组优化空间
        int[][][] dp = new int[2][n+1][target+1];
        dp[0][0][0] = 1;
        int old;
        int now = 0;
        for (int i = 1; i <= l; i++) {
            old = now;
            now = 1 - old;
            for (int j = 0; j <= n; j++) {
                for (int k = 0; k <= target; k++) {
                    dp[now][j][k] = dp[old][j][k];
                    if(j > 0 && k >= arr[i-1]){
                        dp[now][j][k] += dp[old][j-1][k-arr[i-1]];
                    }
                }
            }
        }
        return dp[now][n][target];
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4};
        int n = 2;
        int target = 5;
        int res = kSum(arr, n, target);
        System.out.println(res);
    }
}
