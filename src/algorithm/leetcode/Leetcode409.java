package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 409. 最长回文串
 * 输入:s = "abccccdd"
 * 输出:7
 * @author caoyang
 */
public class Leetcode409 {
    public static int longestPalindrome(String s) {
        char[] chars = s.toCharArray();
        Map<Character, Integer> map = new HashMap<>();
        for (char c : chars) {
            map.put(c, map.getOrDefault(c, 0)+1);
        }
        int odd = 0;
        for (char c : map.keySet()) {
            if ( map.get(c) % 2 != 0){
                odd++;
            }
        }
        return odd <= 1 ? s.length() : s.length() - odd + 1;
    }

    public static void main(String[] args) {
        String s = "abccccdd";
        int result = longestPalindrome(s);
        System.out.println(result);
    }
}
