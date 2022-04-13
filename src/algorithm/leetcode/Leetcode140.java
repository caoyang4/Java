package src.algorithm.leetcode;

import java.util.*;

/**
 * 140. 单词拆分 II
 * 给定一个字符串 s 和一个字符串字典wordDict，在字符串s中增加空格来构建一个句子，使得句子中所有的单词都在词典中。以任意顺序 返回所有这些可能的句子
 *
 * 输入:s = "catsanddog", wordDict = ["cat","cats","and","sand","dog"]
 * 输出:["cats and dog","cat sand dog"]
 *
 * @author caoyang
 */
public class Leetcode140 {
    public static List<String> wordBreak(String s, List<String> wordDict) {
        List<String> result = new ArrayList<>();
        Deque<String> path = new LinkedList<>();
        trackBack(result, path, s, wordDict, 0);
        return result;
    }
    public static void trackBack(List<String> result, Deque<String> path, String s, List<String> wordDict, int start){
        if (start == s.length()){
            result.add(String.join(" ", path));
            return;
        }
        for (int j = start; j < s.length(); j++) {
            if(wordDict.contains(s.substring(start,j+1))) {
                path.add(s.substring(start,j+1));
                trackBack(result, path, s, wordDict,j+1);
                path.removeLast();
            }
        }
    }


    public static void main(String[] args) {
        String s = "catsanddog";
        List<String> wordDict = Arrays.asList("cat","cats","and","sand","dog");
        List<String> result = wordBreak(s, wordDict);
        System.out.println(result);
    }
}
