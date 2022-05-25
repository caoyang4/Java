package src.algorithm.leetcode;

/**
 * 509. 斐波那契数
 * @author caoyang
 */
public class Leetcode509 {
    public static int fib(int n) {
        return (n == 0 || n == 1) ? n : fib(n-1) + fib(n-2);
    }

    public static void main(String[] args) {
        int n = 4;
        int result = fib(n);
        System.out.println(result);
    }
}
