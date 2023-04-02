package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 20. 有效的括号
 * 给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串 s ，判断字符串是否有效。
 * 有效字符串需满足：
 * 左括号必须用相同类型的右括号闭合。
 * 左括号必须以正确的顺序闭合。
 *
 * 输入：s = "()[]{}"
 * 输出：true
 *
 * @author caoyang
 */
public class Leetcode20 {

    /**
     * 栈解法
     * 1、没有足够的右括号;
     * 2、不同种类的右括号;
     * 3、左括号匹配完了, 还有多余的右括号;
     * @param s
     * @return
     */
    public static boolean isValid(String s) {
        if (s == null || s.length() % 2 == 1) {
            return false;
        }
        Deque<Character> deque = new LinkedList<>();
        for (int i = 0; i < s.length(); i++) {
            char bracket = s.charAt(i);
            if(bracket == '('){
                deque.push(')');
            }else if(bracket == '['){
                deque.push(']');
            }else if(bracket == '{'){
                deque.push('}');
            }else if(deque.isEmpty() || deque.peek() != bracket){
                // 情况 2 和 3
                return false;
            }else {
                deque.pop();
            }
        }
        // 情况 1
        return deque.isEmpty();
    }

    /**
     * 匹配解法
     * @param s
     * @return
     */
    public static boolean isValid1(String s) {
        if(s==null ||  s.length() % 2 == 1){
            return false;
        }
        while(s.contains("()") || s.contains("[]") || s.contains("{}")){
            s = s.replace("()","");
            s = s.replace("[]","");
            s = s.replace("{}","");
        }
        return "".equals(s);
    }

    public static void main(String[] args) {
        String s = "{[]}()]";
        System.out.println(isValid1(s));

    }
}
