package src.algorithm.leetcode;

/**
 * 28. 实现strStr()
 * 给你两个字符串haystack和needle ，请你在 haystack 字符串中找出 needle 字符串出现的第一个位置（下标从 0 开始）。
 * 如果不存在，则返回 -1
 *
 * 输入：haystack = "hello", needle = "ll"
 * 输出：2
 *
 * @author caoyang
 */
public class Leetcode28 {
    public static int strStr(String haystack, String needle) {
        if(needle == null || "".equals(needle) || haystack.startsWith(needle)){
            return 0;
        }
        int hLen = haystack.length();
        int nLen = needle.length();
        if(hLen < nLen){
            return -1;
        }
        String original = haystack;
        int index = 0;
        while (!original.startsWith(needle) && original.length() >= nLen){
            original = haystack.substring(++index, hLen);
        }
        return index <= (hLen-nLen) ? index : -1;
    }

    public static void main(String[] args) {
        String haystack = "hello";
        String needle = "ll";
        int res = strStr(haystack, needle);
        System.out.println(res);
    }
}
