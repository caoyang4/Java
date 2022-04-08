package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.List;

/**
 * 120. 三角形最小路径和
 * 给定一个三角形 triangle ，找出自顶向下的最小路径和。
 * 每一步只能移动到下一行中相邻的结点上。相邻的结点在这里指的是下标与上一层结点下标相同或者等于上一层结点下标+1的两个结点。
 * 也就是说，如果正位于当前行的下标 i ，那么下一步可以移动到下一行的下标 i 或 i + 1
 *
 * 输入：triangle = [[2],[3,4],[6,5,7],[4,1,8,3]]
 * 输出：11
 *
 * @author caoyang
 */
public class Leetcode120 {
    public static int minimumTotal(List<List<Integer>> triangle) {
        if (triangle.isEmpty() || triangle.get(0).isEmpty()){
            return 0;
        }
        int len = triangle.size();
        int[] dp = new int[len + 1];
        for (int i = len-1; i >= 0; i--) {
            for (int j = 0; j <= i; j++) {
                dp[j] = Math.min(dp[j], dp[j+1]) + triangle.get(i).get(j);
            }
        }
        return dp[0];
    }


    public static void main(String[] args) {
        List<List<Integer>> triangle = Arrays.asList(
                Arrays.asList(2),
                Arrays.asList(3,4),
                Arrays.asList(6,5,7),
                Arrays.asList(4,1,8,3)
        );
        int result = minimumTotal(triangle);
        System.out.println(result);
    }
}
