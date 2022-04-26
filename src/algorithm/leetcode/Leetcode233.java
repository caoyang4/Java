package src.algorithm.leetcode;

/**
 * 233. 数字 1 的个数
 * 给定一个整数 n，计算所有小于等于 n 的非负整数中数字 1 出现的个数
 * 输入：n = 13
 * 输出：6
 * @author caoyang
 */
public class Leetcode233 {
    public static int countDigitOne(int n) {
        if (n < 1){return 0;}
        return getOnes(1, n);
    }
    public static int getOnes(int start, int end){
        if(start == end){
            return countOnes(start);
        }
        int middle = start + ((end-start) >> 1);
        return getOnes(start, middle) + getOnes(middle+1, end);
    }

    public static int countOnes(int digit){
        char[] chars = String.valueOf(digit).toCharArray();
        int result = 0;
        for (char c : chars) {
            if (c == '1'){result++;}
        }
        return result;
    }

    public static void main(String[] args) {
        int n = 824883294;
        int reuslt = countDigitOne(n);
        System.out.println(reuslt);
    }
}
