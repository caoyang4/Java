package src.algorithm.leetcode;

/**
 * UTF-8 编码验证
 *   1          | 0xxxxxxx
 *   2          | 110xxxxx 10xxxxxx
 *   3          | 1110xxxx 10xxxxxx 10xxxxxx
 *   4          | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
 * @author caoyang
 * @create 2023-05-10 16:45
 */
public class Leetcode393 {
    public static boolean validUtf8(int[] data) {
        int cnt = 0;
        for (int datum : data) {
            if (cnt > 0){
                if ((datum & 0xC0) == 0x80){
                    cnt--;
                } else {
                    return false;
                }
            } else if ((datum & 0xE0) == 0xC0){
                cnt = 1;
            } else if ((datum & 0xF0) == 0xE0){
                cnt = 2;
            } else if ((datum & 0xF8) == 0xF0){
                cnt = 3;
            } else if ((datum & 0x80) != 0){
                return false;
            }
        }
        return cnt == 0;
    }

    public static long getUtfCode(int[] data){
        int cnt = 0;
        long res = 0;
        for (int datum : data) {
            if (cnt > 0){
                if ((datum & 0xC0) == 0x80){
                    cnt--;
                    res = res << 6;
                    long t = datum & 0x3f;
                    res = res | t;
                } else {
                    return -1L;
                }
            } else if ((datum & 0xE0) == 0xC0){
                cnt = 1;
                res = datum & 0x1f;
            } else if ((datum & 0xF0) == 0xE0){
                cnt = 2;
                res = datum & 0x0f;
            } else if ((datum & 0xF8) == 0xF0){
                cnt = 3;
                res = datum & 0x07;
            } else if ((datum & 0x80) == 0){
                return datum;
            } else {
                return -1L;
            }
        }
        return res;

    }

    public static void main(String[] args) {
        int[] data = {197,130,1};
        boolean valid = validUtf8(data);
        System.out.println(valid);
        long code = getUtfCode(data);
        System.out.println(code);
    }
}
