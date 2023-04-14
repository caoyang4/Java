package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 93. 复原IP地址
 * 有效IP地址正好由四个整数（每个整数位于 0 到 255 之间组成，且不能含有前导 0），整数之间用 '.' 分隔。
 * 例如："0.1.2.201" 和 "192.168.1.1" 是 有效 IP 地址，但是 "0.011.255.245"、"192.168.1.312" 和 "192.168@1.1" 是无效IP地址
 *
 * 输入：s = "25525511135"
 * 输出：["255.255.11.135","255.255.111.35"]
 *
 * @author caoyang
 */
public class Leetcode93 {
    public static List<String> restoreIpAddresses(String s) {
        List<String> result = new ArrayList<>();
        char[] digits = s.toCharArray();
        trackBack(result, digits, new LinkedList<>(), 0);
        return result;
    }
    public static void trackBack(List<String> result, char[] digits, Deque<String> ip, int start){
        int len = digits.length;
        if (start == len){
            if (ip.size() == 4){
                result.add(String.join(".", new ArrayList<>(ip)));
            }
            return;
        }

        if (ip.size() >= 4){
            return;
        }

        if (start < len){
            String tmp = digits[start]+"";
            ip.add(tmp);
            trackBack(result, digits, ip, start+1);
            ip.removeLast();
        }
        if (start + 1 < len){
            int num = (digits[start] - '0')*10 + (digits[start+1]-'0');
            if(num >= 10){
                String tmp = ""+digits[start]+digits[start+1];
                ip.add(tmp);
                trackBack(result, digits, ip, start+2);
                ip.removeLast();
            }
        }
        if (start+2 < len){
            int num = (digits[start] - '0')*100 + (digits[start+1]-'0')*10 + (digits[start+2]-'0');
            if(num >= 100 && num <= 255){
                String tmp = ""+digits[start]+digits[start+1]+digits[start+2];
                ip.add(tmp);
                trackBack(result, digits, ip, start+3);
                ip.removeLast();
            }
        }
    }

    public static void main(String[] args) {
        String s = "25525511135";
        List<String> result = restoreIpAddresses(s);
        for (String s1 : result) {
            System.out.println(s1);
        }
    }
}
