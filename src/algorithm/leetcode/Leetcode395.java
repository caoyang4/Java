package src.algorithm.leetcode;

import java.util.*;

/**
 * 395. 至少有K个重复字符的最长子串
 * 给你一个字符串 s 和一个整数 k ，请你找出 s 中的最长子串， 要求该子串中的每一字符出现次数都不少于 k
 * 输入：s = "ababbc", k = 2
 * 输出：5
 * 解释：最长子串为 "ababb" ，其中 'a' 重复了 2 次， 'b' 重复了 3 次。
 * @author caoyang
 */
public class Leetcode395 {
    // 对于字符串 ss，如果存在某个字符ch，它的出现次数大于 00 且小于 kk，则任何包含ch的子串都不可能满足要求。
    // 也就是说，我们将字符串按照ch切分成若干段，则满足要求的最长子串一定出现在某个被切分的段内，而不能跨越一个或多个段
    public static int longestSubstring(String s, int k) {
        if (s.length() < k){return 0;}
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            map.put(s.charAt(i), map.getOrDefault(s.charAt(i), 0)+1);
        }
        for (char c : map.keySet()) {
            if (map.get(c) < k){
                int max = 0;
                for (String t : s.split(String.valueOf(c))) {
                    max = Math.max(max, longestSubstring(t, k));
                }
                return max;
            }
        }
        return s.length();
    }

    public static void main(String[] args) {
        String s = "dababbc";
        int k = 2;
        int result = longestSubstring(s, k);
        System.out.println(result);
    }

}
