package src.algorithm.leetcode;

/**
 * 14. 最长公共前缀
 * 查找字符串数组中的最长公共前缀。
 * 如果不存在公共前缀，返回空字符串 ""
 * 二分归并思想
 * @author caoyang
 */
public class Leetcode14 {
    public static String longestCommonPrefix(String[] strs) {
        int length = strs.length;
        return subCommonPrefix(strs, 0, length - 1);
    }

    public static String subCommonPrefix(String[] strs, int start, int end){
        if(start >= end){
            return strs[start];
        }
        int middle = start + ((end - start) >> 1);
        String formerCommonPrefix = subCommonPrefix(strs, start, middle);
        String latterCommonPrefix = subCommonPrefix(strs, middle+1, end);
        return mergeStr(formerCommonPrefix, latterCommonPrefix);
    }

    public static String mergeStr(String str1, String str2){
        while (!str1.startsWith(str2)){
            // 公共前缀不匹配就让它变短
            str2 = str2.substring(0, str2.length()-1);
        }
        return str2;
    }

    public static void main(String[] args) {
        String [] strs1 = {"flower","flow","flight"};
        String [] strs2 = {"dog","racecar","car"};
        System.out.println(longestCommonPrefix(strs1));
        System.out.println(longestCommonPrefix(strs2));
    }
}
