package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 443. 压缩字符串
 * 输入：chars = ['a','a','b','b','c','c','c']
 * 输出：返回 6 ，输入数组的前 6 个字符应该是：['a','2','b','2','c','3']
 *
 * @author caoyang
 */
public class Leetcode443 {
    public static int compress(char[] chars) {
        int cursor = 0;
        int left = 0;
        int right = 0;
        int len = chars.length;
        while (right < len){
            while (right < len && chars[left] == chars[right]) right++;
            String num = String.valueOf(right-left);
            chars[cursor++] = chars[left];
            if(!"1".equals(num)){
                for (int i = 0; i < num.length(); i++) {
                    chars[cursor++] = num.charAt(i);
                }
            }
            left = right;
        }
        System.out.println(Arrays.toString(chars));
        return cursor;
    }

    public static void main(String[] args) {
//        char[] chars = {'a','a','a','b','b','b','b','b','b','b','b','b','b','b','c','c','c','c','c','c','c','c','c','c','c'};
        char[] chars = {'a','a','a','4','4','b','b','a','c','c','C','C','c','1','2','2'};
        int result = compress(chars);
        System.out.println(result);
    }
}
