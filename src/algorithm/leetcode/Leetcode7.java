package src.algorithm.leetcode;

/**
 * 7. 整数反转
 *
 * 给你一个 32 位的有符号整数 x ，返回将 x 中的数字部分反转后的结果。
 * 如果反转后整数超过 32 位的有符号整数的范围[−2^31, 2^31− 1] ，就返回 0。
 * 假设环境不允许存储 64 位整数（有符号或无符号）
 *
 * @author caoyang
 */
public class Leetcode7 {
    public static int reverse(int x) {
        int result = 0;
        while(x != 0) {
            // 保存计算之前的结果
            int tmp = result;
            result = (result * 10) + (x % 10);
            x /= 10;
            // 将计算之后的结果 / 10，判断是否与计算之前相同，如果不同，证明发生溢出，返回0
            if (result / 10 != tmp) {
                return 0;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int num = -2147483412;
        System.out.println(reverse(num));
    }
}
