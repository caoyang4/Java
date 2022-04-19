package src.algorithm.leetcode;

/**
 * 172. 阶乘后的零
 * 输入：n = 5
 * 输出：1
 * @author caoyang
 */
public class Leetcode172 {
    /**
     * 统计5因子个数
     */
    public static int trailingZeroes(int n) {
        int count = 0;
        while (n > 5){
            n /= 5;
            count += n;
        }
        return count;
    }


    public static void main(String[] args) {
        int n = 30;
        int result = trailingZeroes(n);
        System.out.println(result);
    }
}
