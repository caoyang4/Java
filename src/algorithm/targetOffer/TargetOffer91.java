package src.algorithm.targetOffer;

/**
 * 假如有一排房子，共 n 个，每个房子可以被粉刷成红色、蓝色或者绿色这三种颜色中的一种，你需要粉刷所有的房子并且使其相邻的两个房子颜色不能相同。
 * 当然，因为市场上不同颜色油漆的价格不同，所以房子粉刷成不同颜色的花费成本也是不同的。每个房子粉刷成不同颜色的花费是以一个n x 3的正整数矩阵 costs 来表示的。
 * 例如，costs[0][0] 表示第 0 号房子粉刷成红色的成本花费；costs[1][2]表示第 1 号房子粉刷成绿色的花费，以此类推。
 * 请计算出粉刷完所有房子最少的花费成本。
 *
 * 输入: costs = [[17,2,17],[16,16,5],[14,3,19]]
 * 输出: 10
 * 解释: 将 0 号房子粉刷成蓝色，1 号房子粉刷成绿色，2 号房子粉刷成蓝色。
 *      最少花费: 2 + 5 + 3 = 10。
 *
 * @author caoyang
 */
public class TargetOffer91 {
    /**
     * 3种颜色
     * @param costs
     * @return
     */
    public static int minCost(int[][] costs) {
        int houseNum = costs.length;
        if (houseNum == 0){
            return 0;
        }
        // 从第 0 个到第 n 个房子
        // f.length = costs.length + 1
        int[][] f = new int[houseNum + 1][3];
        f[0][0] = f[0][1] = f[0][2] = 0;
        for (int i = 1; i <= houseNum ; i++) {
            // 第 i-1 栋房子的颜色
            for (int j = 0; j < 3; j++) {
                f[i][j] = Integer.MAX_VALUE;
                // 第 i-2 栋房子的颜色
                for (int k = 0; k < 3; k++) {
                    // 同色，则跳过
                    if(j == k){ continue; }
                    if(f[i-1][k] + costs[i-1][j] < f[i][j]){
                        // costs[i-1][j] 是因为 f.length = costs.length + 1
                        f[i][j] = f[i-1][k] + costs[i-1][j];
                    }
                }
            }
        }
        return Math.min(f[houseNum][0], Math.min(f[houseNum][1], f[houseNum][2]));
    }

    /**
     * K种颜色情况
     * @param costs
     * @return
     */
    public static int minKHouseCost(int[][] costs) {
        if(costs == null || costs.length == 0 || costs[0].length == 0){
            return 0;
        }
        int houseNum = costs.length;
        int colorNum = costs[0].length;
        int[][] f = new int [houseNum+1][colorNum];
        // 第 0 栋房子花费为0
        for (int i = 0; i < colorNum; i++) {
            f[0][i] = 0;
        }

        // 颜色中前 i-1 个房子花费最小值
        int minV;
        int minIndex = 0;
        // 颜色中前 i-1 个房子花费次小值
        int subMinV;
        int subMinIndex = 0;

        for (int i = 1; i <= houseNum; i++) {
            minV = Integer.MAX_VALUE;
            subMinV = Integer.MAX_VALUE;
            // 先找到最小值和次小值
            for (int j = 0; j < colorNum; j++) {
                if(f[i-1][j] < minV){
                    subMinIndex = minIndex;
                    subMinV = minV;
                    minIndex = j;
                    minV = f[i-1][j];
                }else if(f[i-1][j] < subMinV){
                    subMinIndex = j;
                    subMinV = f[i-1][j];
                }
            }
            for (int j = 0; j < colorNum; j++) {
                if(j != minIndex){
                    f[i][j] = f[i-1][minIndex] + costs[i-1][j];
                } else {
                    f[i][j] = f[i-1][subMinIndex] + costs[i-1][j];
                }
            }
        }

        int res = Integer.MAX_VALUE;
        for (int i = 0; i < colorNum; i++) {
            res = Math.min(res, f[houseNum][i]);
        }
        return res;
    }


    public static void main(String[] args) {
        int[][] costs = {{17,2,17}, {16,16,5}, {14,3,19}};
        int minCost = minCost(costs);
        System.out.println(minCost);
    }
}
