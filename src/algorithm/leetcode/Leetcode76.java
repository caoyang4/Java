package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 76. 最小覆盖子串
 * 给定一个字符串 s 、一个字符串 t 。返回 s 中涵盖 t 所有字符的最小子串。
 * 如果 s 中不存在涵盖 t 所有字符的子串，则返回空字符串 ""
 *
 * 输入：s = "ADOBECODEBANC", t = "ABC"
 * 输出："BANC"
 *
 * @author caoyang
 */
public class Leetcode76 {
    /**
     * Map<Character, Integer> window = new HashMap<>();
     * Map<Character, Integer> need = new HashMap<>();
     * //需要的字符传入need
     * for (char c : t.toCharArray()) {
     *     need.put(c, need.getOrDefault(c, 0) + 1);
     * }
     * int left = 0;
     * int right = 0;
     * while (right < s.length()) {
     *     //c是移入窗口的字符
     *     char c = s.charAt(right);
     *     //右边界右移
     *     right++;
     *
     *     //进入窗口的一系列操作.....
     *
     *     //当满足时,需要进行窗口缩减
     *     while (win收缩) {
     *         char d = s.charAt(left);
     *         //左边界右移
     *         left++;
     *
     *         //出窗口的一系列操作.....
     *     }
     * }
     *
     * @param s
     * @param t
     * @return
     */
    public static String minWindow(String s, String t) {
        int sLen = s.length();
        int tLen = t.length();
        if (sLen < tLen) {
            return "";
        }
        Map<Character, Integer> tMap = new HashMap<>();
        Map<Character, Integer> window = new HashMap<>();
        for (char c : t.toCharArray()) {
            tMap.put(c, tMap.getOrDefault(c, 0)+1);
        }
        int start = 0;
        int len = Integer.MAX_VALUE;
        int left = 0;
        int right = 0;
        int size = 0;
        while (right < sLen){
            char c1 = s.charAt(right);
            right++;
            // 进入窗口
            if (tMap.containsKey(c1)){
                window.put(c1, window.getOrDefault(c1, 0) + 1);
                if(tMap.get(c1).equals(window.get(c1))){
                    size++;
                }
            }

            // 满足条件时，窗口缩减
            while (size == tMap.size()){
                if (right - left < len){
                    start = left;
                    len = right - left;
                }
                char c2 = s.charAt(left);
                left++;
                if(tMap.containsKey(c2)){
                    if(tMap.get(c2).equals(window.get(c2))){
                        size--;
                    }
                    window.put(c2, window.get(c2) - 1);
                }
            }
        }
        return len == Integer.MAX_VALUE ? "" : s.substring(start, start+len);
    }

    public static void main(String[] args) {
        String s = "cabwefgewcwaefgcf";
        String t = "cae";
        String result = minWindow(s, t);
        System.out.println(result);
    }
}
