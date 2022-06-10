package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 678. 有效的括号字符串
 * 给定一个只包含三种字符的字符串：（ ，） 和 *，写一个函数来检验这个字符串是否为有效字符串
 * 输入: "(*))"
 * 输出: True
 * @author caoyang
 */
public class Leetcode678 {
    public static boolean checkValidString(String s) {
        Deque<Integer> leftBracket = new LinkedList<>();
        Deque<Integer> star = new LinkedList<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '*'){
                star.push(i);
            } else if (c == '(') {
                leftBracket.push(i);
            } else {
                if (!leftBracket.isEmpty()){
                    leftBracket.pop();
                } else if (! star.isEmpty()){
                    star.pop();
                } else {
                    return false;
                }
            }
        }
        while (!leftBracket.isEmpty() && !star.isEmpty()){
            if (leftBracket.pop() > star.pop()){
                return false;
            }

        }
        return  leftBracket.isEmpty();
    }

    public static void main(String[] args) {
        String s = "*(*";
        boolean result = checkValidString(s);
        System.out.println(result);
    }
}
