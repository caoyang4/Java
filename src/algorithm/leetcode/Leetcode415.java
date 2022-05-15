package src.algorithm.leetcode;

/**
 * 415. 字符串相加
 * 给定两个字符串形式的非负整数 num1 和num2 ，计算它们的和并同样以字符串形式返回
 *
 * 输入：num1 = "456", num2 = "77"
 * 输出："533"
 * @author caoyang
 */
public class Leetcode415 {
    public static String addStrings(String num1, String num2) {
        if ("0".equals(num1) || "0".equals(num2)){
            return "0".equals(num1) ? num2 : num1;
        }
        char[] chars1 = num1.toCharArray();
        char[] chars2 = num2.toCharArray();
        int right1 = chars1.length - 1;
        int right2 = chars2.length - 1;
        StringBuilder builder = new StringBuilder();
        int carry = 0;
        while (right1 >= 0 || right2 >= 0){
            char c1 = right1 >= 0 ? chars1[right1--] : '0';
            char c2 = right2 >= 0 ? chars2[right2--] : '0';
            int sum = (c1 - '0') + (c2 - '0') + carry;
            int remain = sum % 10;
            carry = sum / 10;
            builder.append(remain);
        }
        if (carry > 0){
            builder.append(carry);
        }
        return builder.reverse().toString();
    }

    public static void main(String[] args) {
        String num1 = "456";
        String num2 = "77";
        String result = addStrings(num1, num2);
        System.out.println(result);
    }

}
