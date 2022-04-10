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
        Set<String> wordSet = new HashSet<>(wordList);
        if (wordList.isEmpty() || !wordList.contains(endWord)) { return null; }
        List<List<String>> result = new ArrayList<>();
        wordSet.remove(beginWord);
        // 记录搜索的单词和层数映射关系
        Map<String, Integer> levels = new HashMap<>();
        // beginword为第 0 层
        levels.put(beginWord, 0);
        // 记录单词由其他单词转换得到的映射关系，一对多
        Map<String, Set<String>> map = new HashMap<>();

        boolean found = bfs(beginWord, endWord, wordSet, levels, map);
        // 从 endWord 向 beginWord 倒着搜索
        if (found) {
            Deque<String> path = new LinkedList<>();
            path.add(endWord);
            dfs(beginWord, endWord, path, result, map);
        }
        return result;
    }

    /**
     * BFS构建图
     */
    public static boolean bfs(String beginWord, String endWord, Set<String> wordSet, Map<String, Integer> levels, Map<String, Set<String>> map){
        Queue<String> queue = new LinkedList<>();
        queue.add(beginWord);
        int level = 0;
        boolean found = false;
        while (!queue.isEmpty()){
            level++;
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String word = queue.poll();
                char[] chars = word.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    char c = chars[j];
                    for (char k = 'a'; k <= 'z'; k++) {
                        if (c == k) { continue; }
                        chars[j] = k;
                        String nextWord = String.valueOf(chars);
                        if (levels.getOrDefault(nextWord, -1) == level){ map.get(nextWord).add(word); }
                        if (!wordSet.contains(nextWord)){ continue; }
                        wordSet.remove(nextWord);
                        queue.add(nextWord);
                        map.putIfAbsent(nextWord, new HashSet<>());
                        map.get(nextWord).add(word);
                        levels.put(nextWord, level);
                        if (nextWord.equals(endWord)) {
                            // 由于有多条路径到达 endWord 找到以后不能立即退出，只需要设置 found = true 即可
                            found = true;
                        }
                    }
                    chars[j] = c;
                }
            }
            if (found) { break; }
        }
        return found;
    }

    /**
     * DFS根据图，即记录单词可访问到其他单词的映射关系，搜索路径
     */
    public static void dfs(String beginWord, String endWord, Deque<String> path, List<List<String>> result, Map<String, Set<String>> map){
        if (endWord.equals(beginWord)) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (String word : map.get(endWord)) {
            path.addFirst(word);
            dfs(beginWord, word, path, result, map);
            path.removeFirst();
        }
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
