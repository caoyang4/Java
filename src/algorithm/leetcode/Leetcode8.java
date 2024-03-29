package src.algorithm.leetcode;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 8. 字符串转换整数 (atoi)
 * 请你来实现一个myAtoi(string s)函数，使其能将字符串转换成一个 32 位有符号整数（类似 C/C++ 中的 atoi 函数）。
 *
 * 函数myAtoi(string s) 的算法如下：
 *
 * 读入字符串并丢弃无用的前导空格
 * 检查下一个字符（假设还未到字符末尾）为正还是负号，读取该字符（如果有）。 确定最终结果是负数还是正数。 如果两者都不存在，则假定结果为正。
 * 读入下一个字符，直到到达下一个非数字字符或到达输入的结尾。字符串的其余部分将被忽略。
 * 将前面步骤读入的这些数字转换为整数（即，"123" -> 123， "0032" -> 32）。如果没有读入数字，则整数为 0 。必要时更改符号（从步骤 2 开始）。
 * 如果整数数超过 32 位有符号整数范围 [−2^31, 2^31− 1] ，需要截断这个整数，使其保持在这个范围内。
 * 具体来说，小于 −2^31 的整数应该被固定为 −2^31 ，大于 2^31− 1 的整数应该被固定为 2^31− 1 。
 * 返回整数作为最终结果。
 *
 * 注意：
 * 本题中的空白字符只包括空格字符 ' ' 。
 * 除前导空格或数字后的其余字符串外，请勿忽略任何其他字符。
 *
 * @author caoyang
 */
public class Leetcode8 {
    public static int myAtoi1(String s) {
        char[] chars = s.toCharArray();
        int i = 0;
        int res = 0;
        boolean isPositive = true;
        List<String> list = new ArrayList<>();
        while (i < chars.length && (chars[i] == '+' || chars[i] == '-' || chars[i] == ' ' || Character.isDigit(chars[i]))) {
            if (chars[i] == ' ') {
                if (!list.isEmpty()) {
                    break;
                }
            } else if (chars[i] == '-' || chars[i] == '+') {
                if((i < chars.length - 1 && !Character.isDigit(chars[i+1])) || !list.isEmpty()){
                    break;
                }
            } else if (Character.isDigit(chars[i])) {
                if(list.isEmpty() && i > 0 && chars[i-1] == '-'){ isPositive=false; }
                list.add(chars[i] + "");
            }
            i++;
        }
        if (!list.isEmpty()){
            if (! isPositive){ list.add(0, "-");}
            String string = String.join("", list);
            try {
                res = Integer.parseInt(string);
            } catch (NumberFormatException e){
                res = isPositive ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }
        }
        return res;
    }
    public static int myAtoi(String s) {
        char[] chars = s.trim().toCharArray();
        Deque<Integer> deque = new LinkedList<>();
        boolean isPositive = true;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (i == 0){
                if (c == '-'){
                    isPositive = false;
                } else if (Character.isDigit(c)){
                    deque.add(c-'0');
                }  else if (c != '+'){
                    return 0;
                }
            } else {
                if(!Character.isDigit(c)){ break; }
                deque.add(c-'0');
            }
        }

        while (!deque.isEmpty()){
            if(deque.peek() == 0){
                deque.pop();
            } else {
                break;
            }
        }
        if (deque.isEmpty()){return 0;}
        long res = 0;
        int n = deque.size();
        while (!deque.isEmpty()){
            res += deque.pop() * (long)Math.pow(10, --n);
            if(res > Integer.MAX_VALUE || res < 0){
                return isPositive ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }
        }
        return (int)(isPositive ? res : -res);
    }

    public static void main(String[] args) {
        String s = "-42";
        System.out.println(myAtoi(s));
    }
}
