package src.algorithm.leetcode;

/**
 * 最长公共子序列
 * 输入：text1 = "abcde", text2 = "ace"
 * 输出：3
 * 解释：最长公共子序列是 "ace" ，它的长度为 3 。
 * @author caoyang
 */
public class Leetcode1143 {
    public static int longestCommonSubsequence(String text1, String text2) {
        char[] chars1 = text1.toCharArray();
        char[] chars2 = text2.toCharArray();
        int[][] dp = new int[chars1.length+1][chars2.length+1];
        for (int i = 0; i <= chars1.length; i++) {
            for (int j = 0; j <= chars2.length; j++) {
                if(i == 0 || j == 0){
                    dp[i][j] = 0;
                    continue;
                }
                if (chars1[i-1] == chars2[j-1]){
                    dp[i][j] = dp[i-1][j-1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i-1][j] ,dp[i][j-1]);
                }
            }
        }
        return dp[chars1.length][chars2.length];
    }

    public static String longestConsecutiveCommonSubsequence(String text1, String text2){
        char[] chars1 = text1.toCharArray();
        char[] chars2 = text2.toCharArray();
        int[][] dp = new int[chars1.length+1][chars2.length+1];
        int end = 0;
        int maxLen = 0;
        for (int i = 0; i <= chars1.length; i++) {
            for (int j = 0; j <= chars2.length; j++) {
                if(i == 0 || j == 0){
                    dp[i][j] = 0;
                    continue;
                }
                if (chars1[i-1] == chars2[j-1]){
                    dp[i][j] = dp[i-1][j-1] + 1;
                    if (dp[i][j] > maxLen) {
                        maxLen = dp[i][j];
                        end = i;
                    }
                }
            }
        }
        return text1.substring(end-maxLen, end);
    }

    public static void main(String[] args) {
        String text1 = "abcde";
        String text2 = "ace";
        int maxLen = longestCommonSubsequence(text1, text2);
        System.out.println(maxLen);

        String text3 = "abce";
        String commonSubsequence = longestConsecutiveCommonSubsequence(text1, text3);
        System.out.println(commonSubsequence);
    }
}
