package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 22. 括号生成
 * 数字 n 代表生成括号的对数，请你设计一个函数，用于能够生成所有可能的并且有效的括号组合
 *
 * 输入：n = 3
 * 输出：["((()))","(()())","(())()","()(())","()()()"]
 *
 * @author caoyang
 */
public class Leetcode22 {
    /**
     * 动态规划思路：
     * 当我们清楚所有 i<n 时括号的可能生成排列后，对与 i=n 的情况，我们考虑整个括号排列中最左边的括号。
     * 它一定是一个左括号，那么它可以和它对应的右括号组成一组完整的括号 "( )"，我们认为这一组是相比 n-1 增加进来的括号。
     * 那么，剩下 n-1 组括号有可能在哪呢？
     * 剩下的括号要么在这一组新增的括号内部，要么在这一组新增括号的外部（右侧）。
     * 既然知道了 i<n 的情况，那我们就可以对所有情况进行遍历：
     * "(" + 【i=p时所有括号的排列组合】 + ")" + 【i=q时所有括号的排列组合】
     * 其中 p + q = n-1，且 p q 均为非负整数。
     * 事实上，当上述 p 从 0 取到 n-1，q 从 n-1 取到 0 后，所有情况就遍历完了。
     * 注：上述遍历是没有重复情况出现的，即当 (p1,q1)≠(p2,q2) 时，按上述方式取的括号组合一定不同。
     *
     * @param n
     * @return
     */
    public List<String> generateParenthesis(int n) {
        if (n == 0){
            return new ArrayList<>();
        }
        List<List<String>> result = new ArrayList<>();
        List<String> list0 = new ArrayList<>();
        list0.add("");
        result.add(list0);
        List<String> list1 = new ArrayList<>();
        list1.add("()");
        result.add(list1);

        for (int i = 2; i <= n; i++) {
            List<String> tmp = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                List<String> pList = result.get(j);
                List<String> qList = result.get(i-j-1);
                pList.forEach(p -> qList.forEach(q -> {
                    String r = "(" + p + ")" + q;
                    tmp.add(r);
                }));
            }
            result.add(tmp);
        }

        return result.get(n);
    }


    public static void main(String[] args) {

    }
}
