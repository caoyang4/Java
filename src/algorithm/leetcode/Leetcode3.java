package src.algorithm.leetcode;

import java.util.*;

/**
 * 3. 无重复字符的最长子串
 *
 * 给定一个字符串 s，请你找出其中不含有重复字符的 最长子串 的长度
 * @author caoyang
 */
public class Leetcode3 {
    public static int lengthOfLongestSubstring(String s) {
        char[] chars = s.toCharArray();
        Map<Character, Integer> map = new HashMap<>();
        int max = 0;
        int start = 0;
        for (int i = 0; i < chars.length; i++) {
            if (map.containsKey(chars[i])){
                start = Math.max(start, map.get(chars[i])+1);
            }
            map.put(chars[i], i);
            max = Math.max(max, i-start+1);
        }
        return max;
    }

    public static int lengthOfLongestSubstring1(String s) {
        char[] chars = s.toCharArray();
        List<Character> list = new ArrayList<>();
        int max = 0;
        for (int i = 0; i < chars.length; i++) {
            if (!list.contains(chars[i])){
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
        List<String> testStr = Arrays.asList("abba", "abcabcbb", "pwwkew","bbbbb");
        for (String s : testStr) {
            System.out.println(lengthOfLongestSubstring(s));
        }
    }
}
