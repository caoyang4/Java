package src.algorithm.leetcode;

/**
 * 557. 反转字符串中的单词 III
 *
 * 输入：s = "Let's take LeetCode contest"
 * 输出："s'teL ekat edoCteeL tsetnoc"
 * @author caoyang
 */
public class Leetcode557 {
    public static String reverseWords(String s) {
        String[] words = s.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            StringBuilder tmp = new StringBuilder(word);
            builder.append(tmp.reverse()).append(" ");
        }
        return builder.substring(0, s.length());
    }

    public static void main(String[] args) {
        String s = "Let's take LeetCode contest";
        String result = reverseWords(s);
        System.out.println(result);
    }
}
