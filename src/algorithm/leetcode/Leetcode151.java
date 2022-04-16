package src.algorithm.leetcode;



/**
 * 151. 颠倒字符串中的单词
 * 输入：s = "the sky is blue"
 * 输出："blue is sky the"
 * @author caoyang
 */
public class Leetcode151 {
    public static String reverseWords(String s) {
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = words.length-1; i >= 0; i--) {
            if (!"".equals(words[i].replaceAll(" ",""))){
                sb.append(words[i]).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static void main(String[] args) {
        String s = "the sky is blue";
        String result = reverseWords(s);
        System.out.println(result);
    }
}
