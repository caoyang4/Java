package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 3. 无重复字符的最长子串
 *
 * 给定一个字符串 s，请你找出其中不含有重复字符的 最长子串 的长度
 * @author caoyang
 */
public class Leetcode3 {

    public static int lengthOfLongestSubstring(String s) {
        char[] chars = s.toCharArray();
        List<Character> list = new ArrayList<>();
        int max = 0;
        for (int i = 0; i < chars.length; i++) {
            if(! list.contains(chars[i])){
                list.add(chars[i]);
            } else {
                max = Math.max(max, list.size());
                int index = list.indexOf(chars[i]);
                list = list.subList(index+1, list.size());
                list.add(chars[i]);
            }
        }
        return Math.max(max, list.size());
    }

    public static void main(String[] args) {
        System.out.println(lengthOfLongestSubstring("abcabcbb"));
        System.out.println(lengthOfLongestSubstring("bbbbb"));
        System.out.println(lengthOfLongestSubstring("pwwkew"));
        System.out.println(lengthOfLongestSubstring(""));
        System.out.println(lengthOfLongestSubstring("   b"));
        System.out.println(lengthOfLongestSubstring("dvdf"));
        System.out.println(lengthOfLongestSubstring("aabaab!bb"));
    }
}
