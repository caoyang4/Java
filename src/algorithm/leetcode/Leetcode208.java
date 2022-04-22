package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 208. 实现 Trie (前缀树)
 * @author caoyang
 */
public class Leetcode208 {
    static class Trie {
        List<String> words;
        public Trie() {
            words = new ArrayList<>();
        }

        public void insert(String word) {
            words.add(word);
        }

        public boolean search(String word) {
            return  words.contains(word);
        }

        public boolean startsWith(String prefix) {
            for (String word : words) {
                if (word.startsWith(prefix)){
                    return true;
                }
            }
            return false;
        }
    }

    public static void main(String[] args) {

    }
}
