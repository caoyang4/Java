package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 204. 计数质数
 * 输入：n = 10
 * 输出：4
 * @author caoyang
 */
public class Leetcode204 {
    public static int countPrimes(int n) {
        boolean[] isPrimes = new boolean[n];
        Arrays.fill(isPrimes, true);
        for (int i = 2; i*i < n; i++) {
            if(isPrimes[i]){
                for (int j = i*i; j < n; j+=i) {
                    isPrimes[j] = false;
                }
            }
        }
        int count = 0;
        for (int i = 2; i < isPrimes.length; i++) {
            if(isPrimes[i]) { count++;}
        }
        return count;
    }
    public static int countPrimes1(int n) {
        if (n < 3){ return 0;}
        int count = 0;
        for (int i = 2; i < n; i++) {
            if (isPrimes(i)){ count++; }
        }
        return count;
    }

    public static boolean isPrimes(int n) {
        if (n <= 3){ return n > 1;}
        // 6x-1 或 6x-5 可能为质数
        // 6x, 6x+2, 6x+3, 6x+4都不是质数
        if (n % 6 != 1 && n % 6 != 5){ return false;}
        int sqrt = (int) Math.sqrt(n);
        for (int i = 5; i <= sqrt; i+=6) {
            if (n % i == 0 || n % (i+2) == 0){
                return false;
            }
        }
        return true;
    }



    public static void main(String[] args) {
        int n = 10;
        int reuslt = countPrimes(n);
        System.out.println(reuslt);
    }
}
