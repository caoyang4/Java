package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 43. 字符串相乘
 * 给定两个以字符串形式表示的非负整数 num1 和 num2，返回num1和num2的乘积，它们的乘积也表示为字符串形式。
 * 注意：不能使用任何内置的 BigInteger 库或直接将输入转换为整数
 *
 * 输入: num1 = "2", num2 = "3"
 * 输出: "6"
 *
 * @author caoyang
 */
public class Leetcode43 {
    public static String multiply(String num1, String num2){
        if("0".equals(num1) || "0".equals(num2)){
            return "0";
        }
        char[] chars1 = num1.toCharArray();
        int num1Len = chars1.length;
        char[] chars2 = num2.toCharArray();
        int num2Len = chars2.length;
        List<String> result = new ArrayList<>();
        for (int i = 0; i < chars1.length; i++) {
            if(chars1[i] == '0'){ continue; }
            for (int j = 0; j < chars2.length; j++) {
                if(chars2[j] == '0'){ continue; }
                int zeroLen = num1Len - 1 - i + num2Len - 1 - j;
                int multi =  (chars1[i] - '0') * (chars2[j] - '0');
                result.add(convertStr(multi, zeroLen));
            }
        }

        return mergeNumString(result, 0, result.size()-1);
    }

    public static String mergeNumString(List<String> result, int start, int end){
        if(start >= end){
            return result.get(start);
        }
        int middle = start + ((end - start) >> 1);
        String left = mergeNumString(result, start, middle);
        String right = mergeNumString(result, middle+1, end);
        return mergeString(left, right);
    }

    public static String mergeString(String left, String right){
        char[] s1 = left.toCharArray();
        char[] s2 = right.toCharArray();
        int cursor1 = s1.length-1;
        int cursor2 = s2.length-1;
        int carry = 0;
        StringBuilder res = new StringBuilder();
        while (cursor1 >= 0 && cursor2 >= 0){
            int sum = (s1[cursor1--] - '0') + (s2[cursor2--] - '0') + carry;
            carry = sum / 10;
            int remain = sum % 10;
            res.insert(0, remain);
        }
        while (cursor1 >= 0){
            if(carry > 0){
                int sum = (s1[cursor1--] - '0') + carry;
                carry = sum / 10;
                int remain = sum % 10;
                res.insert(0, remain);
            } else {
                res.insert(0, left.substring(0, cursor1+1));
                break;
            }
        }
        while (cursor2 >= 0){
            if(carry > 0){
                int sum = (s2[cursor2--] - '0') + carry;
                carry = sum / 10;
                int remain = sum % 10;
                res.insert(0, remain);
            } else {
                res.insert(0, right.substring(0, cursor2+1));
                break;
            }
        }
        if(carry > 0){
            res.insert(0, carry);
        }
        return res.toString();
    }

    public static String convertStr(int multi, int zeroLen){
        StringBuilder sb = new StringBuilder(String.valueOf(multi));
        for (int i = 0; i < zeroLen; i++) {
            sb.append("0");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String num1 = "123456789";
        String num2 = "987654321";
        String res = multiply(num1, num2);
        System.out.println(res);
    }
}
