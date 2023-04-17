package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.List;

/**
 * 139. 单词拆分
 * 给你一个字符串 s 和一个字符串列表 wordDict 作为字典。请你判断是否可以利用字典中出现的单词拼接出 s 。
 * 注意：不要求字典中出现的单词全部都使用，并且字典中的单词可以重复使用。
 *
 * 输入: s = "applepenapple", wordDict = ["apple", "pen"]
 * 输出: true
 * @author caoyang
 */
public class Leetcode139 {
    public static boolean wordBreak(String s, List<String> wordDict) {
        int n = s.length();
        boolean[] dp = new boolean[n+1];
        // dp[i]表示 s[0:i] 子串是否能被拼接
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
