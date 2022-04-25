package src.algorithm.leetcode;

import java.util.Stack;

/**
 * 224. 基本计算器
 * s 由数字、'+'、'-'、'('、')'、和 ' ' 组成
 * s 表示一个有效的表达式
 * '+' 不能用作一元运算(例如， "+1"和 "+(2 + 3)"无效)
 * '-' 可以用作一元运算(即 "-1"和 "-(2 + 3)"是有效的)
 *
 * 输入：s = "(1+(4+5+2)-3)+(6+8)"
 * 输出：23
 * @author caoyang
 */
public class Leetcode224 {
    public static int calculate(String s) {
        s = "0"+s.replaceAll(" ","");
        char[] chars = s.toCharArray();
        Stack<String> stack = new Stack<>();
        int index = 0;
        while (index < chars.length){
            char c = chars[index++];
            String t = String.valueOf(c);
            if(c == '('){
                stack.add(t);
            } else if(c == '+' || c == '-'){
                if(chars[index] == '('){
                    stack.add(t);
                } else {
                    StringBuilder digit = new StringBuilder();
                    while (index < chars.length && Character.isDigit(chars[index])){
                        digit.append(chars[index++]);
                    }
                    String d = digit.toString();
                    if (c == '-'){d = "-"+d;}
                    stack.add(d);
                }
            } else if (c == ')'){
                int r = 0;
                while (!stack.isEmpty()){
                    String p = stack.pop();
                    if("(".equals(p)){
                        break;
                    } else {
                        r += Integer.parseInt(p);
                    }
                }
                String d = String.valueOf(r);
                if (!stack.isEmpty()){
                   if(!"(".equals(stack.peek()) && "-".equals(stack.pop())){
                       d = r > 0 ? "-"+d : String.valueOf(-r);
                   }
                }
                stack.add(d);
            } else {
                StringBuilder digit = new StringBuilder(t);
                while (index < chars.length && Character.isDigit(chars[index])){
                    digit.append(chars[index++]);
                }
                stack.add(digit.toString());
            }
        }

        int result = 0;
        while (!stack.isEmpty()){
            result += Integer.parseInt(stack.pop());
        }
        return result;
    }

    public static void main(String[] args) {
        String s ="2-(5-6)";
        int result = calculate(s);
        System.out.println(result);
    }
}
