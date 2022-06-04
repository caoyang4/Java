package src.algorithm.leetcode;

/**
 * 647. 回文子串
 * 输入：s = "aaa"
 * 输出：6
 * 解释：6个回文子串: "a", "a", "a", "aa", "aa", "aaa"
 * @author caoyang
 */
public class Leetcode647 {
    // 一维动态规划
    public static int countSubstrings1(String s) {
        int len = s.length();
        // dp[i] 表示 s[0,i] 的回文子串个数
        int[] dp = new int[len + 1];
        for (int i = 1; i <= len; i++) {
            dp[i] = dp[i-1];
            for (int j = 0; j < i; j++) {
                if(isPalindrome(s, j, i-1)){
                    dp[i] += 1;
                }
            }
        }
        return dp[len];
    }
    // 二维动态规划
    public static int countSubstrings2(String s) {
        int len = s.length();
        // dp[i][j] 表示 s[i,j] 是否是回文串
        boolean[][] dp = new boolean[len][len];
        char[] chars = s.toCharArray();
        int count = 0;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j <= i; j++) {
                if (chars[i] == chars[j] && (i - j < 2 || dp[j+1][i-1])){
                    dp[j][i] = true;
                    count++;
                }
            }
        }
        return count;
    }
    // 中心扩展
    public static int countSubstrings(String s) {
        int len = s.length();
        char[] chars = s.toCharArray();
        int count = 0;
        for (int i = 0; i < 2 * len - 1; i++) {
            int left = i >> 1;
            int right = left + (i & 1);
            while (left >= 0 && right < len && chars[left] == chars[right]){
                count++;
                // 向左扩展
                left--;
                // 向右扩展
                right++;
            }
        }
        return count;
    }
    public static boolean isPalindrome(String s, int start, int end){
        while (start < end){
            if(s.charAt(start++) != s.charAt(end--)){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String s = "aaa";
        int result = countSubstrings(s);
        System.out.println(result);
    }
}
