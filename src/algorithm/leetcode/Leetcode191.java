package src.algorithm.leetcode;

/**
 * 191. 位1的个数
 * 输入：11111111111111111111111111111101
 * 输出：31
 * @author caoyang
 */
public class Leetcode191 {
    public static int hammingWeight(int n) {
        int count = 0;
        while (n != 0) {
            n = n & (n-1);
            count++;
        }
        return count;
    }
    public static int hammingWeight1(int n) {
        int count = 0;
        while (n != 0){
            count += n & 1;
            // 无符号右移一位
            n >>>= 1;
        }
        return count;
    }


    public static void main(String[] args) {
        int n = 3;
        int reuslt = hammingWeight(n);
        System.out.println(reuslt);
    }
}
