package src.algorithm.leetcode;

import java.util.*;

/**
 * 127. 单词接龙
 * 广度优先搜索 BFS
 * 字典wordList中从单词beginWord和endWord的转换序列是一个按下述规格形成的序列beginWord -> s1-> s2-> ... -> sk：
 * 每一对相邻的单词只差一个字母。
 * 对于1 <= i <= k时，每个si都在wordList中。注意，beginWord不需要在wordList中。
 * sk==endWord
 * 给你两个单词beginWord和endWord和一个字典wordList ，返回从beginWord到endWord的最短转换序列中的单词数目
 *
 * 输入：beginWord = "hit", endWord = "cog", wordList = ["hot","dot","dog","lot","log","cog"]
 * 输出：5
 *
 * @author caoyang
 */
public class Leetcode127 {
    /**
     * 转换为无向图结构，找最短距离
     *               dot -- dog
     *             /  |      |   \
     *   hit -- hot   |      |    \--- cog
     *            \   |      |   /
     *              lot -- log  /
     * 单向 BFS
     */
    public static int ladderLengthBySingleDirection(String beginWord, String endWord, List<String> wordList) {
        Set<String> wordSet = new HashSet<>(wordList);
        if(wordSet.isEmpty() || !wordSet.contains(endWord)){
            return 0;
        }
        // 去除首节点
        wordSet.remove(beginWord);
        Queue<String> queue = new LinkedList<>();
        queue.add(beginWord);
        // 已遍历过的元素
        Set<String> visited = new HashSet<>();
        // 起始距离为1
        int step = 1;
        while (!queue.isEmpty()) {
            int currSize = queue.size();
            for (int i = 0; i < currSize; i++) {
                String word = queue.poll();
                char[] chars = word.toCharArray();
                for (int j = 0; j < chars.length; j++) {
                    char c = chars[j];
                    for (char k = 'a'; k <= 'z'; k++) {
                        if (c == k){ continue;}
                        chars[j] = k;
                        String nextWord = String.valueOf(chars);
                        if (wordSet.contains(nextWord)){
                            if (nextWord.equals(endWord)){
                                return step + 1;
                            }
                            if (!visited.contains(nextWord)){
                                queue.add(nextWord);
                                // 添加到队列后，要马上标记为已访问
                                visited.add(nextWord);
                            }
                        }
                    }
                    chars[j] = c;
                }
            }
            step++;
        }
        return 0;
    }

    public static int ladderLengthByDoubleDirection(String beginWord, String endWord, List<String> wordList) {
        Set<String> wordSet = new HashSet<>(wordList);
        if (wordSet.isEmpty() || !wordSet.contains(endWord)){
            return 0;
        }
        // 第 2 步：已经访问过的 word 添加到 visited 哈希表里
        Set<String> visited = new HashSet<>();

        Set<String> beginVisited = new HashSet<>();
        beginVisited.add(beginWord);
        Set<String> endVisited = new HashSet<>();
        endVisited.add(endWord);
        int step = 1;
        while (!beginVisited.isEmpty() && !endVisited.isEmpty()){
            if(beginVisited.size() > endVisited.size()){
                Set<String> tmp = beginVisited;
                beginVisited = endVisited;
                endVisited = tmp;
            }
            // 保证 beginVisited 是相对较小的集合，nextLevelVisited 在扩散完成以后，会成为新的 beginVisited
            Set<String> nextLevelVisited = new HashSet<>();
            for (String word : beginVisited) {
                char[] chars = word.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    char c = chars[i];
                    for (char j = 'a'; j <= 'z'; j++) {
                        if (c == j) { continue; }
                        chars[i] = j;
                        String nextWord = String.valueOf(chars);
                        if(wordSet.contains(nextWord)){
                            if(endVisited.contains(nextWord)){
                                return step + 1;
                            }
                            if(!visited.contains(nextWord)){
                                visited.add(nextWord);
                                nextLevelVisited.add(nextWord);
                            }
                        }
                    }
                    chars[i] = c;
                }
            }
            beginVisited = nextLevelVisited;
            step++;
        }
        return 0;
    }


    public static void main(String[] args) {
        String beginWord = "hit";
        String endWord = "cog";
        List<String> wordList = Arrays.asList("hot","dot","dog","lot","log","cog");
        int result1 = ladderLengthBySingleDirection(beginWord, endWord, wordList);
        int result2 = ladderLengthByDoubleDirection(beginWord, endWord, wordList);
        System.out.println(result1);
        System.out.println(result2);
    }
}
