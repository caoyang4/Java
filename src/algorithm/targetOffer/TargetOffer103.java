package src.algorithm.targetOffer;

/**
 * 剑指offer 103
 * 给定不同面额的硬币 coins 和一个总金额 amount。编写一个函数来计算可以凑成总金额所需的最少的硬币个数。如果没有任何一种硬币组合能组成总金额，返回-1。
 * 你可以认为每种硬币的数量是无限的。
 * 输入：coins = [1, 2, 5], amount = 11
 * 输出：3
 * 解释：11 = 5 + 5 + 1
 * 动态规划问题
 *
 * @author caoyang
 */
public class TargetOffer103 {
    public static int coinChange(int[] coins, int amount) {
        // 保存 0, 1, 2,..., M 的所需最少硬币数
        // f(0), f(10),...f(i),...f(M)
        // 转移方程：f(i) = min{f(i - coins[0])+1, f(i - coins[1])+1,...,f(i - coins[-1])+1}
        int[] f = new int[amount + 1];
        f[0] = 0;
        for (int i = 1; i <= amount; i++) {
            f[i] = Integer.MAX_VALUE;
            for (int coin : coins) {
                if (i >= coin && f[i - coin] != Integer.MAX_VALUE && (f[i - coin] + 1) < f[i]) {
                    f[i] = f[i - coin] + 1;
                }
            }
        }
        return f[amount] != Integer.MAX_VALUE ? f[amount] : -1;
    }

    public static void main(String[] args) {
        int[] coins = {2, 5, 7};
        int amount = 27;
        int minCoinNum = coinChange(coins, amount);
        System.out.println(minCoinNum);
    }
}
