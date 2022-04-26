package src.algorithm.leetcode;

/**
 * 231. 2 的幂
 * 输入：n = 16
 * 输出：true
 * 解释：24 = 16
 * @author caoyang
 */
public class Leetcode231 {
    public static boolean isPowerOfTwo(int n) {
        if (n < 2){ return n == 1; }
        int x = 2;
        while (x < n){
            if(x * 2 < 0){return false;}
            x = x * 2;
        }
        return x == n;
    }

    public static void main(String[] args) {
        int n = 512;
        boolean result = isPowerOfTwo(n);
        System.out.println(result);
    }
}
