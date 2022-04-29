package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 264. 丑数 II
 * 给你一个整数 n ，请你找出并返回第 n 个 丑数
 * 丑数 就是只包含质因数 2、3 和/或 5 的正整数
 * @author caoyang
 */
public class Leetcode264 {
    public static int nthUglyNumber(int n) {
        int[] dp = new int[n+1];
        dp[1] = 1;
        int x = 1, y = 1, z = 1;
        for (int i = 2; i <= n; i++) {
            int num1 = dp[x] * 2;
            int num2 = dp[y] * 3;
            int num3 = dp[z] * 5;
            dp[i] = Math.min(Math.min(num1, num2), num3);
            if(dp[i] == num1){x++;}
            if(dp[i] == num2){y++;}
            if(dp[i] == num3){z++;}
        }
        return dp[n];
    }

    public static void main(String[] args) {
        int n = 1600;
        int reuslt = nthUglyNumber(n);
        System.out.println(reuslt);
    }
}
