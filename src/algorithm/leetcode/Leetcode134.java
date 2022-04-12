package src.algorithm.leetcode;

/**
 * 134. 加油站
 * 在一条环路上有 n 个加油站，其中第 i 个加油站有汽油 gas[i] 升。
 * 你有一辆油箱容量无限的的汽车，从第 i 个加油站开往第 i+1 个加油站需要消耗汽油 cost[i] 升。你从其中的一个加油站出发，开始时油箱为空。
 * 给定两个整数数组 gas 和 cost ，如果你可以绕环路行驶一周，则返回出发时加油站的编号，否则返回 -1
 *
 * 输入: gas = [1,2,3,4,5], cost = [3,4,5,1,2]
 * 输出: 3
 *
 * @author caoyang
 */
public class Leetcode134 {
    /**
     * 环形贪心
     * 分为两个部分：前半程跑不过，后半程跑得过
     */
    public static int canCompleteCircuit(int[] gas, int[] cost) {
        int n = gas.length;
        int total = 0;
        int rest = 0;
        int start = 0;
        for (int i = 0; i < n; i++) {
            total = total + gas[i]-cost[i];
            rest = rest + gas[i]-cost[i];
            if (rest < 0){
                // 重新从第 i+1 个站开始算
                start = i+1;
                rest = 0;
            }
        }
        return total < 0 ? -1 : start;
    }




    public static void main(String[] args) {
        int[] gas = {5,1,2,3,4};
        int [] cost = {4,4,1,5,1};
        int result = canCompleteCircuit(gas, cost);
        System.out.println(result);
    }
}
