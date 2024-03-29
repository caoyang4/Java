package src.algorithm.leetcode;

/**
 * 10. 正则表达式匹配
 *
 * 给你一个字符串 s 和一个字符规律 p，请你来实现一个支持 '.' 和 '*'的正则表达式匹配。
 * '.' 匹配任意单个字符
 * '*' 匹配零个或多个前面的那一个元素
 * 所谓匹配，是要涵盖整个字符串s的，而不是部分字符串。
 *
 * s 只含小写英文字母。
 * p 只含小写英文字母，以及字符 . 和 *。
 * 保证每次出现字符 * 时，前面都匹配到有效的字符
 *
 * @author caoyang
 */
public class Leetcode10 {
    /**
     *  思路：动态规划， 定义二维dp数组，其中dp[i][j]表示s的前i个字符和p的前j个字符是否匹配，
     *  为了方便初始化，我们将s和p的长度均+1
     *  考虑到P中可能出现三种字符：普通字母(a-z)、'*'或者是'.', 则其动态转移方程分别是：
     *    1) 如果p[j]为普通字母，dp[i][j]==dp[i-1][j-1] and s[i]==p[j]
     *    2) 如果p[j]为'.', dp[i][j]==dp[i-1][j-1]
     *    3) 如果p[j]为'*', 则情况比较复杂, 分以下两种情况讨论：
     *        A. 以s="c", p="ca*"为例，此时'*'匹配0次，dp[i][j]==dp[i][j-2]
     *        B. 以s="caa", p="ca*"为例，此时'*'匹配多次，dp[i][j]==dp[i-1][j] and s[i]==p[j-1] (考虑到通配符'.', 还有p[j-1]=='.'的情况)
     *
     */
    public static boolean isMatch(String s, String p) {
        int m = s.length();
        int n = p.length();
        char[] chars1 = s.toCharArray();
        char[] chars2 = p.toCharArray();
        // dp[i][j]表示 s[0:i] 与 p[0:j] 是否匹配
        boolean[][] dp = new boolean[m+1][n+1];
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 && j == 0){
                    // 空串互相匹配
                    dp[i][j] = true;
                    continue;
                }
                if(j == 0){
                    dp[i][j] = false;
                    continue;
                }
                if(chars2[j-1] != '*'){
                    // p[i-1]!='*'时，判断p[i-1]是否为'.'，或是否两两相等
                    if(i > 0 && (chars1[i-1] == chars2[j-1] || chars2[j-1] == '.')){
                        dp[i][j] |= dp[i-1][j-1];
                    }
                } else {
                    if (j > 1){
                        // *表示0 个字符，直接忽略，形如 "a" 与 "ab*"
                        dp[i][j] |= dp[i][j-2];
                        // p[i-1]='*'时，判断前一个字符是否是'.'，或是否两两匹配
                        if (i > 0 && (chars1[i-1] == chars2[j-2] || chars2[j-2] == '.')){
                            dp[i][j] |= dp[i-1][j];
                        }
                    }

                }
            }
        }
        return dp[m][n];
    }


    public static void main(String[] args) {
        String s = "ab";
        String p = ".*c";
        System.out.println(isMatch(s, p));
    }
}
