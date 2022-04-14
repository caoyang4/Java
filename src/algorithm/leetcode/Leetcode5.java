package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 5. 最长回文子串
 * 给你一个字符串 s，找到 s 中最长的回文子串
 * @author caoyang
 */
public class Leetcode5 {
    public static String longestPalindrome(String s) {
        String result = "";
        if(s == null){ return result; }
        char[] chars = s.toCharArray();
        int n = chars.length;
        for (int i = 0; i < n; i++) {
            int start = i;
            int end = i;
            // 奇数回文串
            while (start >= 0 && end < n && chars[start] == chars[end]){
                String tmp = s.substring(start--, ++end);
                result = tmp.length() > result.length() ? tmp : result;
            }
            start = i;
            end = i+1;
            // 偶数回文串
            while (start >= 0 && end < n && chars[start] == chars[end]){
                String tmp = s.substring(start--, ++end);
                result = tmp.length() > result.length() ? tmp : result;
            }
        }
        return result;
    }
    public static String longestPalindrome1(String s) {
        if(s == null){ return ""; }
        char[] chars = s.toCharArray();
        if(chars.length <= 1){ return s; }
        String res = "";
        for (int i = chars.length - 1; i > 0; i--) {
            int k = i;
            int j = 0;
            String temp = chars[i] + "";
            List<Integer> left = new ArrayList<>();
            while(j < i){
                if (chars[k] == chars[j]){
                    left.add(j);
                    if (k == j || k == (j+1)){
                        temp = new String(Arrays.copyOfRange(chars, left.get(0), i+1));
                        break;
                    }
                    k--;
                } else {
                    // 中途发现不相等，k 回到 i 位置，
                    // 若 left 不为空；j 回到 left[0] 位置，清空 left。处理形如 "aaabaa"
                    k = i;
                    if(! left.isEmpty()) { j = left.get(0);}
                    left = new ArrayList<>();
                }
                j++;
            }
            res = res.length() >= temp.length() ? res : temp;
        }
        return res;
    }

    public static void main(String[] args) {
        String test = "aaabaad";
        System.out.println(longestPalindrome(test));
    }
}
