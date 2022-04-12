package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 135. 分发糖果
 * n 个孩子站成一排。给你一个整数数组 ratings 表示每个孩子的评分。
 * 你需要按照以下要求，给这些孩子分发糖果：
 * 每个孩子至少分配到 1 个糖果。
 * 相邻两个孩子评分更高的孩子会获得更多的糖果。
 * 请你给每个孩子分发糖果，计算并返回需要准备的 最少糖果数目
 *
 * 输入：ratings = [1,0,2]
 * 输出：5
 * @author caoyang
 */
public class Leetcode135 {
    /**
     * 贪心算法
     */
    public static int candy(int[] ratings) {
        int n = ratings.length;
        int[] candy = new int[n];
        Arrays.fill(candy, 1);
        // 从左往右计算评分高于前面孩子的糖果数量
        for (int i = 1; i < n; i++) {
            if(ratings[i] > ratings[i-1]){
                candy[i] = candy[i-1] + 1;
            }
        }
        // 从右往左计算评分高于后面孩子的糖果数量
        for (int i = n-2; i >= 0; i--) {
            if(ratings[i] > ratings[i+1]){
                candy[i] = Math.max(candy[i], candy[i+1]+1);
            }
        }
        return Arrays.stream(candy).sum();
    }

    public static void main(String[] args) {
        int[] ratings = {1, 0, 2};
        int result = candy(ratings);
        System.out.println(result);
    }
}
