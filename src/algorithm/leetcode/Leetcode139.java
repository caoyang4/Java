package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.List;

/**
 * 139. 单词拆分
 *
 * 输入: s = "applepenapple", wordDict = ["apple", "pen"]
 * 输出: true
 * @author caoyang
 */
public class Leetcode139 {
    public static boolean wordBreak(String s, List<String> wordDict) {
        int n = s.length();
        boolean[] dp = new boolean[n+1];
        for (int i = 1; i <= n; i++) {
            // wordDict含有当整个子串
            if (wordDict.contains(s.substring(0, i))){
                dp[i] = true;
                continue;
            }
            for (int j = i-1; j > 0; j--) {
                dp[i] = dp[j] && wordDict.contains(s.substring(j, i));
                if (dp[i]) { break; }
            }
        }
        return dp[n];
    }


    public static void main(String[] args) {
        String s = "ddadddbdddadd";
        List<String> wordDict = Arrays.asList("dd","ad","da","b");
        boolean result = wordBreak(s, wordDict);
        System.out.println(result);
    }
}
