package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 402. 移掉K位数字
 *
 * 输入：num = "1432219", k = 3
 * 输出："1219"
 * @author caoyang
 */
public class Leetcode402 {
    public static String removeKdigits(String num, int k) {
        int size = num.length();
        if (size <= k) { return "0"; }
        Deque<Character> deque = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            char c = num.charAt(i);
            // 构造单调栈
            while (k > 0 && !deque.isEmpty() && deque.getLast() > c){
                deque.pollLast();
                k--;
            }
            deque.add(c);
        }
        // 还有剩余的数需要去除，从栈尾部拿掉即可
        for (int i = 0; i < k; i++) {
            deque.pollLast();
        }
        // 去除前导0
        while (!deque.isEmpty() && deque.peek() == '0'){
            deque.pop();
        }

        StringBuilder builder = new StringBuilder();
        while (!deque.isEmpty()){
            builder.append(deque.pop());
        }
        String res = builder.toString();
        return "".equals(res) ? "0" : res;
    }

    public static void main(String[] args) {
        String num = "1432219";
        int k = 3;
        String result = removeKdigits(num, k);
        System.out.println(result);

    }
}
