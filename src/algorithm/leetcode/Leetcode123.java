package src.algorithm.leetcode;

/**
 * 给定一个数组，它的第 i 个元素是一支给定的股票在第 i 天的价格。
 * 设计一个算法来计算你所能获取的最大利润。你最多可以完成两笔交易。
 * 你不能同时参与多笔交易，即必须卖光后才能买入，但可以同一天卖完后，再买入
 *
 *输入：prices = [3,3,5,0,0,3,1,4]
 * 输出：6
 * 解释：在第 4 天（股票价格 = 0）的时候买入，在第 6 天（股票价格 = 3）的时候卖出，这笔交易所能获得利润 = 3-0 = 3 。
 *      随后，在第 7 天（股票价格 = 1）的时候买入，在第 8 天 （股票价格 = 4）的时候卖出，这笔交易所能获得利润 = 4-1 = 3 。
 *
 * @author caoyang
 */
public class Leetcode123 {
    /**
     * 分为 5 个阶段
     * 阶段一、第一次买之前
     * 阶段二、持有股票
     * 阶段三、第一次卖之后，第二次买之前
     * 阶段四、持有股票
     * 阶段五、第二次卖之后
     *
     * @param prices
     * @return
     */
    public static int maxProfit(int[] prices) {
        if(prices == null && prices.length == 1){
            return 0;
        }
        int[][] f = new int[prices.length+1][5+1];
        // 前 0 天
        // 阶段一
        f[0][1] = 0;
        f[0][2] = f[0][3] = f[0][4] = f[0][5] = Integer.MIN_VALUE;
        for (int i = 1; i <= prices.length; i++) {
            // 一三五阶段，手中无股票
            // f[i][j] = max{f[i-1][j], f[i-1][j-1] + p[i-1] - p[i-2]}
            for (int j = 1; j <= 5; j += 2) {
                f[i][j] = f[i-1][j];
                if(i >= 2 && j >= 2 && f[i-1][j-1] != Integer.MIN_VALUE){
                    f[i][j] = Math.max(f[i][j], f[i-1][j-1] + prices[i-1] - prices[i-2]);
                }
            }
            // 二四阶段，手中持有股票
            // f[i][j] = max{f[i-1][j] + p[i-1] - p[i-2], f[i-1][j-1], f[i-1][j-2] + p[i-1] - p[i-2] }
            for (int j = 2; j <= 4; j+=2) {
                f[i][j] = f[i-1][j-1];
                if(i > 1 && f[i-1][j] != Integer.MIN_VALUE){
                    f[i][j] = Math.max(f[i][j], f[i-1][j] + prices[i-1] - prices[i-2]);
                }
                if(i > 1 && j > 2 && f[i-1][j-2] != Integer.MIN_VALUE){
                    f[i][j] = Math.max(f[i][j], f[i-1][j-2] + prices[i-1] - prices[i-2]);
                }
            }
        }
        return Math.max(Math.max(f[prices.length][1], f[prices.length][3]),f[prices.length][5]);
    }

    public static void main(String[] args) {
        int[] prices = {3,3,5,0,0,3,1,4};
        int res = maxProfit(prices);
        System.out.println(res);
    }
}
