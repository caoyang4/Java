package src.algorithm.leetcode;

/**
 * 6. Z 字形变换
 *
 * 将一个给定字符串 s 根据给定的行数 numRows ，以从上往下、从左到右进行 Z 字形排列
 * @author caoyang
 */
public class Leetcode6 {
    public static String convert(String s, int numRows) {
        int sLen = s.length();
        if(sLen == 1 || numRows == 1){
            return s;
        }

        char[] chars = s.toCharArray();
        String[][] strTwoArr = new String[numRows][sLen];
        int k = 0;
        int j = 0;
        while (k < sLen){
            for (int i = 0; i < numRows; i++) {
                if (j % (numRows - 1) == 0 || ((i + j) % (numRows - 1) == 0)){
                    if (k == sLen){
                        return convertStr(strTwoArr);
                    }
                    strTwoArr[i][j] = chars[k++] + "";
                    if (j % (numRows - 1) != 0) { break; }
                }
            }
            j++;
        }

        return convertStr(strTwoArr);
    }

    public static String convertStr(String[][] strTwoArr){
        StringBuilder sb = new StringBuilder();
        for (int m = 0; m < strTwoArr.length; m++){
            for (int n = 0; n < strTwoArr[m].length; n++) {
                if(strTwoArr[m][n] != null){
                    sb.append(strTwoArr[m][n]);
                }
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        /**
         * num = 3 PAHNAPLSIIGYIR
         * num = 4 PINALSIGYAHRPI
         */
        String s = "PAYPALISHIRING";
        int numRows = 4;
        System.out.println(convert(s, numRows));
    }
}
