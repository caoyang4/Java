package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 17. 电话号码的字母组合
 * 给定一个仅包含数字 2-9 的字符串，返回所有它能表示的字母组合。答案可以按任意顺序返回。
 * 给出数字到字母的映射如下（与电话按键相同）。注意 1 不对应任何字母。
 *
 * 输入：digits = "23"
 * 输出：["ad","ae","af","bd","be","bf","cd","ce","cf"]
 *
 * 回溯算法
 *
 * @author caoyang
 */
public class Leetcode17 {

    public static List<String> letterCombinations(String digits) {
        List<String> list = new ArrayList<>();
        if (digits == null || digits.length() == 0) {
            return list;
        }
        //初始对应所有的数字，为了直接对应2-9，新增了两个无效的字符串""
        String[] numString = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        backTrack(list, new StringBuilder(), digits, numString, 0);
        return list;
    }


    public static void backTrack(List<String> list, StringBuilder tmp, String digits, String[] numString, int start) {
        if(start == digits.length()){
            list.add(tmp.toString());
            return;
        }
        char[] alphas = numString[digits.charAt(start)-'0'].toCharArray();
        for (char alpha : alphas) {
            tmp.append(alpha);
            backTrack(list, tmp, digits, numString, start+1);
            tmp.delete(tmp.length()-1, tmp.length());
        }
    }

    public static void main(String[] args) {
        String digits = "23";
        List<String> res = letterCombinations(digits);
        for (String re : res) {
            System.out.println(re);
        }
    }
}
