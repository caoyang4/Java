package src.algorithm.leetcode;

import java.util.*;

/**
 * 126. 单词接龙 II
 * BFS + DFS
 * 广度优先搜索
 * 按字典wordList完成从单词 beginWord 到单词 endWord 转化，一个表示此过程的 转换序列是形式上像 beginWord -> s1 -> s2 -> ... -> sk 这样的单词序列，并满足：
 * 每对相邻的单词之间仅有单个字母不同。
 * 转换过程中的每个单词 si（1 <= i <= k）必须是字典wordList中的单词。注意，beginWord不必是字典wordList中的单词。
 * sk == endWord
 * 给你两个单词 beginWord 和 endWord ，以及一个字典 wordList 。请你找出并返回所有从beginWord到endWord的最短转换序列 ，如果不存在这样的转换序列，返回一个空列表。
 * 每个序列都应该以单词列表 [beginWord, s1, s2, ..., sk] 的形式返回
 *
 * 输入：beginWord = "hit", endWord = "cog", wordList = ["hot","dot","dog","lot","log","cog"]
 * 输出：[["hit","hot","dot","dog","cog"],["hit","hot","lot","log","cog"]]
 *
 * @author caoyang
 */
public class Leetcode126 {
    /**
     * 转换为无向图结构，找最短距离
     *               dot -- dog
     *             /  |      |   \
     *   hit -- hot   |      |    \--- cog
     *            \   |      |   /
     *              lot -- log  /
     */
    public static List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
        return null;
    }

    public static void main(String[] args) {
        String beginWord = "hit";
        String endWord = "cog";
        List<String> wordList = Arrays.asList("hot","dot","dog","lot","log","cog");
        List<List<String>> result = findLadders(beginWord, endWord, wordList);
        for (List<String> stringList : result) {
            System.out.println(stringList);
        }
    }
}
