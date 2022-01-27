package src.algorithm.leetcode;

/**
 * 9. 回文数
 *
 * 给你一个整数 x ，如果 x 是一个回文整数，返回 true ；否则，返回 false 。
 * 回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。例如，121 是回文，而 123 不是。
 * @author caoyang
 */
public class Leetcode9 {

    /**
     * 转为字符数组
     */
    public static boolean isPalindrome(int x) {
        if (x < 0 ){ return false; }
        char[] chars = String.valueOf(x).toCharArray();
        int i = 0;
        int j = chars.length - 1;
        while (i <= j){
            if(chars[i] != chars[j]){
                return false;
            }
            i++;
            j--;
        }
        return true;
    }

    /**
     * 反转是否相等
     */
    public static boolean isPalindrome1(int x) {
        if (x < 0){ return false; }
        int res = 0;
        int tmp = x;
        while (tmp != 0){
            res = res * 10 + tmp % 10;
            tmp /= 10;
        }
        return res == x;
    }

    public static void main(String[] args) {
        int x = 0;
        System.out.println(isPalindrome1(x));
    }

}
