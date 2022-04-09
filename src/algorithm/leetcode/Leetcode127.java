package src.algorithm.leetcode;

import java.util.List;

/**
 * 127. 单词接龙
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
    public int num = 0;
    public int ladderLength(String beginWord, String endWord, List<String> wordList) {
        if(!wordList.contains(endWord)){
            return 0;
        }

        for (int i = 0; i < endWord.length(); i++) {
            char[] chars = beginWord.toCharArray();
            chars[i] = endWord.charAt(i);
            if(wordList.contains(String.valueOf(chars))){
                beginWord = String.valueOf(chars);
                break;
            }
        }
        return 0;
    }

    public static void main(String[] args) {

    }
}
