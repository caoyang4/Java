package src.algorithm.leetcode;

/**
 * 91. 解码方法
 *
 * 一条包含字母A-Z 的消息通过以下映射进行了 编码 ：
 * 'A' -> "1"
 * 'B' -> "2"
 * ...
 * 'Z' -> "26"
 * 要解码已编码的消息，所有数字必须基于上述映射的方法，反向映射回字母（可能有多种方法）。例如，"11106" 可以映射为：
 * "AAJF" ，将消息分组为 (1 1 10 6)
 * "KJF" ，将消息分组为 (11 10 6)
 * 注意，消息不能分组为(1 11 06) ，因为 "06" 不能映射为 "F" ，这是由于 "6" 和 "06" 在映射中并不等价。
 * 给你一个只含数字的非空字符串 s ，请计算并返回解码方法的总数 。
 *
 * @author caoyang
 */
public class Leetcode91 {
    /**
     * 每段字母划分成若干个数字，对应一个字母
     * 即前 n-1 个数字解密方式数与前 n-2 个数字解密方式数
     * f(i) = f(i-1)[对应 s[i-1] 个字母] + f(i-2)[对应 s[i-1]与 s[i-2] 两个字母]
     * f(0) = 1, 即解密为空串
     * @param s
     * @return
     */
    public static int numDecodings(String s) {
        int sLen = s.length();
        if(sLen == 0){ return 0; }
        int[] f = new int[sLen+1];
        f[0] = 1;
        char[] chars = s.toCharArray();
        for (int i = 1; i <= sLen; i++) {
            f[i] = 0;
            int t1 = chars[i - 1] - '0';
            // 单字母情况
            if (t1 >= 1 && t1 <= 9){ f[i] += f[i-1]; }
            // 长度大于1时，10-26 可以合并为字母，即双字母情况
            if(i >= 2 && chars[i-2] != '0') {
                int t2 = (chars[i-2] - '0')*10 + t1;
                if(t2 <= 26){ f[i] += f[i-2]; }
            }
        }
        return f[sLen];
    }

    public static void main(String[] args) {
        String s = "12";
        int res = numDecodings(s);
        System.out.println(res);
    }
}
