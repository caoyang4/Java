package src.algorithm.leetcode;

/**
 * 背包问题 3
 * 物品不限量
 * @author caoyang
 */
public class LeetcodeBackPack3 {
    /**
     * 物品不限量
     * @param m 背包容量
     * @param W 物品重量
     * @param V 物品价值
     * @return
     */
    public static int backPack3(int m, int[] W, int[] V){
        if(W == null || W.length == 0){ return 0; }
        int[] dp = new int[m+1];
        dp[0] = 0;
        for (int i = 1; i <= m; i++) {
            dp[i] = -1;
        }
        for (int i = 1; i <= W.length; i++) {
            for (int j = 0; j <= m; j++) {
                if(j >= W[i-1] && dp[j - W[i-1]] != -1){
                    dp[j] = Math.max(dp[j], dp[j - W[i-1]]+V[i-1]);
                }
            }
        }


        int res = 0;
        for (int i = 1; i <= m; i++) {
            if(dp[i] != -1){
                res = Math.max(res, dp[i]);
            }
        }
        return res;

    }
    public static void main(String[] args) {
        int[] W = {2,3,5,7};
        int[] V = {1,5,2,4};
        int m = 10;
        int res = backPack3(m, W, V);
        System.out.println(res);
    }
}
