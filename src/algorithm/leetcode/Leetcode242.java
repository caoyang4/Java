package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 242. 有效的字母异位词
 * 给定两个字符串 s 和 t ，编写一个函数来判断 t 是否是 s 的字母异位词。
 * 注意：若s 和 t中每个字符出现的次数都相同，则称s 和 t互为字母异位词
 *
 * 输入: s = "anagram", t = "nagaram"
 * 输出: true
 * @author caoyang
 */
public class Leetcode242 {
    public static boolean isAnagram(String s, String t) {
        if(s.length() != t.length()){return false;}
        Map<Character, Integer> map = new HashMap<>();
        char[] chars = s.toCharArray();
        char[] chart = t.toCharArray();
        for (char sc : chars) {
            map.put(sc, map.getOrDefault(sc, 0) + 1);
        }
        for (char tc : chart) {
            int times = map.getOrDefault(tc, 0);
            if(times == 0){return false;}
            map.put(tc,--times);
        }
        return true;
    }

    public static void main(String[] args) {
        String s = "car", t = "rat";
        boolean result = isAnagram(s, t);
        System.out.println(result);
    }
}
