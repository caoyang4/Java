package src.algorithm.targetOffer;

/**
 * 最少回文分割
 * 给定一个字符串 s，请将 s 分割成一些子串，使每个子串都是回文串。
 * 返回符合要求的 最少分割次数
 *
 * 输入：s = "aab"
 * 输出：1
 * @author caoyang
 */
public class TargetOffer94 {
    public static int minCut(String s) {
        if (s==null) { return 0; }
        char[] chars = s.toCharArray();
        int size = chars.length;
        // boolean[i][j]表示 i-j 是否是回文串
        boolean[][] isPalindrome = new boolean[size][size];
        // dp[i]表示前 i 个字符切割成回文串的最少次数
        int[] dp = new int[size+1];
        int i, j;
        for (int t = 0; t < size; t++) {
            i = j = t;
            // i-j 是否构成奇数回文串
            while (i >= 0 && j < size && chars[i] == chars[j]){
                isPalindrome[i--][j++] = true;
            }
            // i-j 是否构成偶数回文串
            i = t;
            j = t+1;
            while (i >= 0 && j < size && chars[i] == chars[j]){
                isPalindrome[i--][j++] = true;
            }
        }

        for (i = 1; i <= size; i++) {
            dp[i] = i;
            for (j = 0; j < i; j++) {
                if(isPalindrome[j][i-1]){
                    dp[i] = Math.min(dp[j]+1, dp[i]);
                }
            }
        }
        return dp[size] - 1;
    }

    public static void main(String[] args) {
        String s = "aaba";
        int res = minCut(s);
        System.out.println(res);

    }
}
