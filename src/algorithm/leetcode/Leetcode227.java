package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 227. 基本计算器 II
 * 输入：s = "3+2*2"
 * 输出：7
 *
 * 1 <= s.length <= 3 * 105
 * s 由整数和算符 '+', '-', '*', '/' 组成，中间由一些空格隔开
 * s 表示一个 有效表达式
 *
 * @author caoyang
 */
public class Leetcode227 {
    public static int calculate(String s) {
        s = s.replaceAll(" ","");
        char[] chars = s.toCharArray();
        // 存放数字
        Deque<Integer> numbers = new LinkedList<>();
        // 存放操作符
        Deque<Character> operators = new LinkedList<>();
        int index = 0;
        while (index < chars.length){
            char c = chars[index++];
            if (Character.isDigit(c)){
                StringBuilder digits = new StringBuilder(c+"");
                while (index < chars.length && Character.isDigit(chars[index])){
                    digits.append(chars[index++]);
                }
                numbers.add(Integer.parseInt(digits.toString()));
            } else {
                if (c == '*' || c == '/'){
                    int x = numbers.removeLast();
                    StringBuilder digits = new StringBuilder();
                    while (index < chars.length && Character.isDigit(chars[index])){
                        digits.append(chars[index++]);
                    }
                    int y = Integer.parseInt(digits.toString());
                    int r = c == '*' ? x*y : x/y;
                    numbers.add(r);
                } else {
                    operators.add(c);
                }
            }
        }
        if (operators.isEmpty()){ return numbers.pop(); }
        int result = numbers.pop();
        while (!numbers.isEmpty()){
            char op = operators.pop();
            int y = numbers.pop();
            result = op == '+' ? result+y : result-y;
        }
        return result;
    }

    public static void main(String[] args) {
        String s = "1-1+1+2*3";
        int result = calculate(s);
        System.out.println(result);
    }
}
