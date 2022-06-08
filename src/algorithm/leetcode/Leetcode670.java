package src.algorithm.leetcode;

/**
 * 670. 最大交换
 * 给定一个非负整数，你至多可以交换一次数字中的任意两位。返回你能得到的最大值
 * 输入: 2736
 * 输出: 7236
 * @author caoyang
 */
public class Leetcode670 {
    public static int maximumSwap(int num) {
        char[] chars = String.valueOf(num).toCharArray();
        int n = chars.length;

        for (int i = 0; i < n-1; i++) {
            int t = n - 1;
            char max = chars[i];
            for (int j = n-1; j > i; j--) {
                if (chars[j] > max){
                    t = j;
                    max = chars[j];
                }
            }
            if (max > chars[i]){
                chars[t] = chars[i];
                chars[i] = max;
                break;
            }
        }
        return Integer.parseInt(String.valueOf(chars));
    }

    public static void main(String[] args) {
        int num = 27367;
        int result = maximumSwap(num);
        System.out.println(result);
    }
}
