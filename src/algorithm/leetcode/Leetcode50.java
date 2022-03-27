package src.algorithm.leetcode;

/**
 * 50. Pow(x, n)
 *
 * 输入：x = 2.00000, n = 10
 * 输出：1024.00000
 * @author caoyang
 */
public class Leetcode50 {
    public static double myPow1(double x, int n) {
        if (Math.abs(x) == 1.0){
            return n % 2 == 1 ? x : Math.abs(x);
        }
        if (n == 0){
            return 1.0;
        }
        double result = x;
        if(n > 0){
            for (int i = 0; i < n - 1; i++) {
                result *= x;
            }
        } else {
            // Integer 溢出
            if(n == Math.abs(n)){
                return 0.0;
            }
            for (int i = n; i <= 0; i++) {
                result /= x;
            }
        }
        return result;
    }

    /**
     * 使用折半计算，每次把n缩小一半，这样n最终会缩小到0，任何数的0次方都为1，这时候我们再往回乘，
     * 如果此时n是偶数，直接把上次递归得到的值算个平方返回即可，如果是奇数，则还需要乘上个x的值
     * @param x
     * @param n
     * @return
     */
    public static double myPow(double x, int n) {
        double res = 1.0;
        for(int i = n; i != 0; i /= 2){
            if(i % 2 != 0){
                res *= x;
            }
            x *= x;
        }
        return  n < 0 ? 1 / res : res;
    }

    public static void main(String[] args) {
        double x = 2.10000;
        int n = 3;
        double result = myPow(x, n);
        System.out.println(result);

    }
}
