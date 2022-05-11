package src.algorithm.leetcode;

/**
 * 400. 第N位数字
 *
 * 给你一个整数 n ，请你在无限的整数序列 [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, ...] 中找出并返回第 n 位上的数字
 *
 * 输入：n = 11
 * 输出：0
 * 解释：第 11 位数字在序列 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, ... 里是 0 ，它是 10 的一部分
 * @author caoyang
 */
public class Leetcode400 {
    public static int findNthDigit(int n) {
        long start = 1;
        long multiple = 1;
        long remain = n;
        while ((remain = remain - 9 * multiple * start) > 0){
            start++;
            // 此处可能存在int溢出，故需要 long 类型
            multiple *= 10;
        }

        if (remain == 0){
            return 9;
        } else{
            remain +=  9 * multiple * start;
            long nthNum = remain / start;
            long nthDigit = remain % start;
            if (nthDigit == 0){
                String num = String.valueOf(multiple+nthNum-1);
                return Integer.parseInt(num.substring(num.length()-1));
            } else {
                String num = String.valueOf(multiple+nthNum);
                return Integer.parseInt(num.substring((int)nthDigit-1, (int)nthDigit));
            }
        }
    }

    public static void main(String[] args) {
        int n = 2147483647;
        int result = findNthDigit(n);
        System.out.println(result);
    }
}
