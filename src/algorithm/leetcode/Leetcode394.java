package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 394. 字符串解码
 * 输入：s = "3[a]2[bc]"
 * 输出："aaabcbc"
 * @author caoyang
 */
public class Leetcode394 {
    // 辅助栈
    public static String decodeString(String s) {
        StringBuilder result = new StringBuilder();
        Deque<Integer> multiple = new LinkedList<>();
        Deque<String> strings = new LinkedList<>();
        int times = 0;
        for (char c : s.toCharArray()) {
            if (c == '['){
                multiple.add(times);
                strings.add(result.toString());
                result = new StringBuilder();
                times = 0;
            } else if(c == ']'){
                int multi = multiple.removeLast();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < multi; i++) {
                    builder.append(result);
                }
                result = new StringBuilder(strings.removeLast()+builder);
            } else if (c >= '0' && c <= '9'){
                times = 10 * times + (c - '0');
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String s = "3[z]2[2[y]pq4[2[jk]e1[f]]]ef";
        String result = decodeString(s);
        System.out.println(result);
    }
}
