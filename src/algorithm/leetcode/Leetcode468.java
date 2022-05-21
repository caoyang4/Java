package src.algorithm.leetcode;


/**
 * 468. 验证IP地址
 * 输入：queryIP = "172.16.254.1"
 * 输出："IPv4"
 * @author caoyang
 */
public class Leetcode468 {
    public static String validIPAddress(String queryIP) {
        String[] ipv4 = queryIP.split("\\.");
        String[] ipv6 = queryIP.split(":");
        if(queryIP.endsWith(":") || queryIP.endsWith(".")){
            return "Neither";
        }
        if (ipv4.length == 4){
            for (String ip : ipv4) {
                if (!validIpv4(ip)){
                    return "Neither";
                }
            }
            return "IPv4";
        } else if (ipv6.length == 8){
            for (String ip : ipv6) {
                if (!validIpv6(ip)){
                    return "Neither";
                }
            }
            return "IPv6";
        }
        return "Neither";
    }
    public static boolean validIpv4(String ip){
        int len = ip.length();
        if (len < 1 || len > 3){
            return false;
        }
        if(len > 1 && ip.charAt(0) == '0'){return false;}
        for (int i = 0; i < len; i++) {
            if(!Character.isDigit(ip.charAt(i))){return false;}
        }
        return Integer.parseInt(ip) <= 255;
    }

    public static boolean validIpv6(String ip){
        int len = ip.length();
        if (len < 1 || len > 4){
            return false;
        }
        for (int i = 0; i < len; i++) {
            char c = ip.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String queryIP = "2001:0db8:85a3:0:0:8A2E:0370:7334";
        String result = validIPAddress(queryIP);
        System.out.println(result);
    }
}
