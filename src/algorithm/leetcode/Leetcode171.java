package src.algorithm.leetcode;

/**
 * 171. Excel 表列序号
 * 输入: columnTitle = "AB"
 * 输出: 28
 * @author caoyang
 */
public class Leetcode171 {
    public static int titleToNumber(String columnTitle) {
        char[] chars = columnTitle.toCharArray();
        int n = chars.length;
        int result = 0;
        for (int i = 0; i < n; i++) {
            int tmp = chars[i] - 64;
            for (int j = i; j < n-1; j++) {
                tmp *= 26;
            }
            result += tmp;
        }
        return result;
    }

    public static void main(String[] args) {
        String columnTitle = "ZY";
        int result = titleToNumber(columnTitle);
        System.out.println(result);
    }
}
