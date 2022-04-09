package src.algorithm.leetcode;

/**
 * 125. 验证回文串
 * @author caoyang
 */
public class Leetcode125 {
    public static boolean isPalindrome(String s) {
        int start = 0;
        int end = s.length() - 1;

        while (start < end){
            boolean startIsAlpha = Character.isLetter(s.charAt(start)) || Character.isDigit(s.charAt(start));
            boolean endIsAlpha = Character.isLetter(s.charAt(end)) || Character.isDigit(s.charAt(end));
            if (!startIsAlpha){ start++; }
            if (!endIsAlpha){ end--; }
            if(startIsAlpha && endIsAlpha){
                if(!(String.valueOf(s.charAt(start))).equalsIgnoreCase(String.valueOf(s.charAt(end)))){
                    return false;
                }
                start++;
                end--;
            }

        }
        return true;
    }

    public static void main(String[] args) {
        String s = "0P";
        boolean result = isPalindrome(s);
        System.out.println(result);
    }
}
