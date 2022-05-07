package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 344. 反转字符串
 * 输入：s = ["h","e","l","l","o"]
 * 输出：["o","l","l","e","h"]
 * @author caoyang
 */
public class Leetcode344 {
    public static void reverseString(char[] s) {
        int start = 0;
        int end = s.length-1;
        while (start < end){
            char head = s[start];
            s[start++] = s[end];
            s[end--] = head;
        }
    }

    public static void main(String[] args) {
        char[] s = {'h','e', 'l', 'l', 'o'};
        reverseString(s);
        System.out.println(Arrays.toString(s));
    }
}
