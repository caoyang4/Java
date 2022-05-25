package src.algorithm.leetcode;

/**
 * 504. 七进制数
 * 输入: num = 100
 * 输出: "202"
 * @author caoyang
 */
public class Leetcode504 {
    public static String convertToBase7(int num) {
        if (num == 0) return "0";
        int n = Math.abs(num);
        StringBuilder builder = new StringBuilder();
        while (n != 0){
            int remain = n % 7;
            n /= 7;
            builder.append(remain);
        }
        if (num < 0) builder.append("-");
        return builder.reverse().toString();
    }

    public static void main(String[] args) {
        int num = -7;
        String result = convertToBase7(num);
        System.out.println(result);
    }
}
