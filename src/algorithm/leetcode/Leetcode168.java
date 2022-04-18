package src.algorithm.leetcode;

/**
 * 168. Excel表列名称
 * 26进制转换
 * 输入：columnNumber = 28
 * 输出："AB
 * @author caoyang
 */
public class Leetcode168 {
    public static String convertToTitle(int columnNumber) {
        char[] alpha = {'Z', 'A', 'B', 'C', 'D',
                        'E', 'F', 'G', 'H', 'I',
                        'J', 'K', 'L', 'M', 'N',
                        'O', 'P', 'Q', 'R', 'S',
                        'T', 'U', 'V', 'W', 'X', 'Y'};
        StringBuilder result = new StringBuilder();
        while (columnNumber != 0){
            int div = columnNumber % 26;
            result.insert(0, alpha[div]);
            if(columnNumber == 26){ break; }
            columnNumber = alpha[div] != 'Z' ? columnNumber/26 : columnNumber/26 - 1;
        }
        return result.toString();
    }

    public static void main(String[] args) {
        int columnNumber = 701;
        String result = convertToTitle(columnNumber);
        System.out.println(result);
    }
}
