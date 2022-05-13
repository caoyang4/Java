package src.algorithm.leetcode;


/**
 * 405. 数字转换为十六进制数
 * @author caoyang
 */
public class Leetcode405 {
    public static String toHex(int num) {
        if (num == 0){return "0";}
        long n = num;
        if (n < 0){
            n = (long)(Math.pow(2,32)) + num;
        }
        StringBuilder builder = new StringBuilder();
        while (n != 0){
            long remain = n % 16;
            char c = (char) (remain + '0');
            if(remain >= 10){
                c = (char) (remain - 10 + 'a');
            }
            builder.insert(0,c);
            n /= 16;
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        int n = -1;
        String result = toHex(n);
        System.out.println(result);
    }
}
