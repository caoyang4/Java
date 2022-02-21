package src.algorithm.targetOffer;

/**
 * 给定一个无符号整数（以二进制串的形式），返回其二进制表达式中数字位数为 '1' 的个数（也被称为 汉明重量).）
 *
 * 输入：n = 11 (控制台输入 00000000000000000000000000001011)
 * 输出：3
 * 解释：输入的二进制串 00000000000000000000000000001011中，共有三位为 '1'
 *
 * @author caoyang
 */
public class TargetOffer15 {
    /**
     * treat n as an unsigned value
     * n & (n-1) 可以每次将最右边的1消去，经过x次运算即可
     * @param n
     * @return
     */
    public static int hammingWeight(int n) {
        int count = 0;
        while (n != 0){
            n &= n-1;
            count++;
        }
        return count;
    }

    public static void main(String[] args) {
        System.out.println(hammingWeight(11));
    }
}
