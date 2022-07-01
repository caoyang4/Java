package src.algorithm.leetcode;

import java.util.*;

/**
 * 692. 前K个高频单词
 * 给定一个单词列表words和一个整数 k ，返回前k个出现次数最多的单词。
 * 返回的答案应该按单词出现频率由高到低排序。如果不同的单词有相同出现频率， 按字典顺序 排序
 *
 * 输入: words = ["i", "love", "leetcode", "i", "love", "coding"], k = 2
 * 输出: ["i", "love"]
 *
 * @author caoyang
 */
public class Leetcode692 {
    public static List<String> topKFrequent(String[] words, int k) {
        Map<String, Integer> map = new HashMap<>();
        for (String word : words) {
            map.put(word, map.getOrDefault(word, 0) + 1);
        }
        List<String> result = new ArrayList<>(map.keySet());
        result.sort((word1, word2) -> {
            int count1 = map.get(word1);
            int count2 = map.get(word2);
            return count1 == count2 ? word1.compareTo(word2) : count2-count1;
        });
        return result.subList(0, k);
    }

    public static void main(String[] args) {
        String[] words = {"i", "love", "leetcode", "i", "love", "coding"};
        int k = 2;
        List<String> result = topKFrequent(words, k);
        System.out.println(result);
    }
}
