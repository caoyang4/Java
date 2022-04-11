package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 131. 分割回文串
 *
 * 输入：s = "aab"
 * 输出：[["a","a","b"],["aa","b"]]
 * @author caoyang
 */
public class Leetcode131 {
    public static List<List<String>> partition(String s) {
        List<List<String>> result = new ArrayList<>();
        if(s == null || "".equals(s)){
            return result;
        }
        char[] chars = s.toCharArray();
        trackBack(result, new LinkedList<>(), s, chars, 0);
        return result;

    }
    public static void trackBack(List<List<String>> result, Deque<String> deque, String s, char[] chars, int start){
        if (start > chars.length){
            return;
        } else if (start == chars.length){
            result.add(new ArrayList<>(deque));
            return;
        }

        for (int i = start; i < chars.length; i++) {
            if(isPalindrome(chars, start, i)){
                deque.add(s.substring(start, i+1));
                trackBack(result, deque,s, chars, i+1);
                deque.removeLast();
            }
        }

    }

    public static boolean isPalindrome(char[] chars,int start, int end){
        while (start < end){
            if(chars[start++] != chars[end--]){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String s = "aab";
        List<List<String>> result = partition(s);
        for (List<String> stringList : result) {
            System.out.println(stringList);
        }
    }
}
