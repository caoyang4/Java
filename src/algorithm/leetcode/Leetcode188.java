package src.algorithm.leetcode;

/**
 * 188. 买卖股票的最佳时机 IV
 * 给定一个整数数组prices ，它的第 i 个元素 prices[i] 是一支给定的股票在第 i 天的价格。
 * 设计一个算法来计算你所能获取的最大利润。你最多可以完成 k 笔交易。
 * 注意：你不能同时参与多笔交易（你必须在再次购买前出售掉之前的股票）
 *
 * 输入：k = 2, prices = [3,2,6,5,0,3]
 * 输出：7
 * 解释：在第 2 天 (股票价格 = 2) 的时候买入，在第 3 天 (股票价格 = 6) 的时候卖出, 这笔交易所能获得利润 = 6-2 = 4 。
 *      随后，在第 5 天 (股票价格 = 0) 的时候买入，在第 6 天 (股票价格 = 3) 的时候卖出, 这笔交易所能获得利润 = 3-0 = 3
 *
 * @author caoyang
 */
public class Leetcode188 {
    public static int maxProfit(int k, int[] prices) {
        if (prices == null || k == 0 || prices.length < 2){
            return 0;
        }
        // 极端情况股票价格 1 2 1 2 1 2，最多需要 n/2 + 1次购买，即买-卖-买-卖往复
        if (k > (prices.length >> 1)){
            int result = 0;
            for (int i = 1; i < prices.length; i++) {
                result += Math.max(prices[i] - prices[i-1], 0);
            }
            return result;
        }
        int[][] f = new int[prices.length+1][2*k+1+1];
        // 前 0 天
        f[0][1] = 0;
        for (int i = 2; i <= 2*k+1; i++) {
            f[0][i] = Integer.MIN_VALUE;
        }

        for (int i = 1; i <= prices.length; i++) {
            // 奇数阶段，手中无股票
            // f[i][j] = max{f[i-1][j], f[i-1][j-1] + p[i-1] - p[i-2]}
            for (int j = 1; j <= 2*k+1; j += 2) {
                f[i][j] = f[i-1][j];
                if(i >= 2 && j >= 2 && f[i-1][j-1] != Integer.MIN_VALUE){
                    f[i][j] = Math.max(f[i][j], f[i-1][j-1] + prices[i-1] - prices[i-2]);
                }
            }
            // 偶数阶段，手中持有股票
            // f[i][j] = max{f[i-1][j] + p[i-1] - p[i-2], f[i-1][j-1], f[i-1][j-2] + p[i-1] - p[i-2] }
            for (int j = 2; j <= 2*k; j+=2) {
                f[i][j] = f[i-1][j-1];
                if(i > 1 && f[i-1][j] != Integer.MIN_VALUE){
                    f[i][j] = Math.max(f[i][j], f[i-1][j] + prices[i-1] - prices[i-2]);
                }
                if(i > 1 && j > 2 && f[i-1][j-2] != Integer.MIN_VALUE){
                    f[i][j] = Math.max(f[i][j], f[i-1][j-2] + prices[i-1] - prices[i-2]);
                }
            }
        }

        int result = Integer.MIN_VALUE;
        for (int i = 1; i <= 2*k+1; i+=2) {
            result = Math.max(result, f[prices.length][i]);
        }
        return result;
    }

    public static void main(String[] args) {
        int[] prices = {3,2,6,5,0,3};
        int k = 2;
        int res = maxProfit(k, prices);
        System.out.println(res);
    }

}
