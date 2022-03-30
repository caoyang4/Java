package src.algorithm.leetcode;

/**
 * 69. x 的平方根
 * 给你一个非负整数 x ，计算并返回 x 的算术平方根 。
 * 由于返回类型是整数，结果只保留整数部分 ，小数部分将被舍去 。
 * 注意：不允许使用任何内置指数函数和算符，例如 pow(x, 0.5) 或者 x ** 0.5
 *
 * 输入：x = 8
 * 输出：2
 *
 * @author caoyang
 */
public class Leetcode69 {
    public static int mySqrt(int x) {
        return calSqrt(x,0, x);
    }
    public static int calSqrt(int x, int start, int end){
        int middle = start + ((end - start) >> 1);
        if ((double)middle * (double)middle > x){
            return calSqrt(x, start, middle);
        } else if (middle*middle < x){
            if ((double)(middle+1) * (double)(middle+1) > x){
                return middle;
            }
            return calSqrt(x, middle+1, end);
        } else {
            return middle;
        }
    }

    public static void main(String[] args) {
        int x = 2147395599;
        int result = mySqrt(x);
        System.out.println(result);
    }
}
