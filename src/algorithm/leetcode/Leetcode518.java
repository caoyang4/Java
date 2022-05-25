package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 518. 零钱兑换 II
 *
 * 给你一个整数数组 coins 表示不同面额的硬币，另给一个整数 amount 表示总金额。
 * 请你计算并返回可以凑成总金额的硬币组合数。如果任何硬币组合都无法凑出总金额，返回 0 。
 * 假设每一种面额的硬币有无限个
 *
 * 输入：amount = 5, coins = [1, 2, 5]
 * 输出：4
 * @author caoyang
 */
public class Leetcode518 {
    // 动态规划
    public static int change(int amount, int[] coins) {
        int[] dp = new int[amount+1];
        dp[0] = 1;
        for (int coin : coins) {
            // 保证不会重复
            for (int i = coin; i <= amount; i++) {
                dp[i] += dp[i-coin];
            }
        }
        return dp[amount];
    }

    // DFS
    public static int change1(int amount, int[] coins) {
        Arrays.sort(coins);
        return  trackBack(amount, coins, 0);
    }
    public static int trackBack(int amount, int[] coins, int start){
        if (amount == 0){
            return 1;
        }
        int count = 0;
        for (int i = start; i < coins.length; i++) {
            if (amount < coins[i]) break;
            count += trackBack(amount-coins[i], coins, i);
        }
        return count;
    }





    public static void main(String[] args) {
        int amount = 100;
        int[] coins = {3,5,7,8,9,10,11};
        int result = change(amount, coins);
        System.out.println(result);
    }
}
