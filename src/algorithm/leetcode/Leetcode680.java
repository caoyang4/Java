package src.algorithm.leetcode;

/**
 * 680. 验证回文字符串 Ⅱ
 * 给定一个非空字符串 s，最多删除一个字符。判断是否能成为回文字符串
 * 输入: s = "abca"
 * 输出: true
 * @author caoyang
 */
public class Leetcode680 {
    public static boolean validPalindrome(String s) {
        char[] chars = s.toCharArray();
        int start = 0;
        int end = chars.length - 1;
        while (start < end){
            if (chars[start] == chars[end]){
                start++;
                end--;
            } else {
                return start + 1 == end || isPalindrome(chars, start+1, end) || isPalindrome(chars, start, end-1);
            }
        }
        return true;
    }
    public static boolean isPalindrome(char[] chars, int start, int end){
        while (start < end){
            if (chars[start++] != chars[end--]){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String s = "cupuufxoohdfpgjdmysgvhmvffcnqxjjxqncffvmhvgsymdjgpfdhooxfuupucu";
        boolean result = validPalindrome(s);
        System.out.println(result);
    }
}
