package src.algorithm.leetcode;

/**
 * 背包问题 1
 * @author caoyang
 */
public class LeetcodeBackPack1 {
    /**
     * @param m 背包容量
     * @param A 物品重量列表
     * @return
     */
    public int backPack(int m, int[] A){
        if(A == null || A.length == 0 || m == 0){
            return 0;
        }
        // dp[i][j] 表示前 i 个物品能否组合成 j 重量
        boolean[][] dp = new boolean[A.length+1][m+1];
        dp[0][0] = true;
        for (int i = 1; i <= A.length; i++) {
            for (int j = 0; j <= m; j++) {
                dp[i][j] = dp[i-1][j];
                if (A[i-1] <= j){
                    dp[i][j] = dp[i][j] || dp[i-1][j-A[i-1]];
                }
            }
        }
        for (int i = m; i > 0; i--) {
            if (dp[A.length][i]){
                return i;
            }
        }
        return 0;
        
    }
}
