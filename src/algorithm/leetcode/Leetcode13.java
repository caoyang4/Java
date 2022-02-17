package src.algorithm.leetcode;

/**
 * 13. 罗马数字转整数
 *
 * 罗马数字包含以下七种字符: I， V， X， L，C，D 和 M
 * 字符          数值
 * I             1
 * V             5
 * X             10
 * L             50
 * C             100
 * D             500
 * M             1000
 *
 * 通常情况下，罗马数字中小的数字在大的数字的右边。但也存在特例，
 * 例如 4 不写做IIII，而是IV。数字 1 在数字 5 的左边，所表示的数等于大数 5 减小数 1 得到的数值 4 。同样地，数字 9 表示为IX。
 *
 * 这个特殊的规则只适用于以下六种情况：
 * I可以放在V(5) 和X(10) 的左边，来表示 4 和 9。
 * X可以放在L(50) 和C(100) 的左边，来表示 40 和 90。
 * C可以放在D(500) 和M(1000) 的左边，来表示400 和900。
 * 给定一个罗马数字，将其转换成整数。
 *
 * @author caoyang
 */
public class Leetcode13 {
    public static int romanToInt(String s) {
        int res = 0;
        int i = 0;
        char[] romaChars = s.toCharArray();
        while (i < romaChars.length) {
            if(romaChars[i] == 'I'){
                if(i < romaChars.length - 1 && romaChars[i+1] == 'V'){
                    res += 4;
                    i += 2;
                } else if (i < romaChars.length - 1 && romaChars[i+1] == 'X'){
                    res += 9;
                    i += 2;
                } else {
                    res += 1;
                    i++;
                }
            }else if(romaChars[i] == 'V'){
                res += 5;
                i++;
            }else if(romaChars[i] == 'X'){
                if(i < romaChars.length - 1 && romaChars[i+1] == 'L'){
                    res += 40;
                    i += 2;
                } else if (i < romaChars.length - 1 && romaChars[i+1] == 'C'){
                    res += 90;
                    i += 2;
                } else {
                    res += 10;
                    i++;
                }
            }else if(romaChars[i] == 'L'){
                res += 50;
                i++;
            }else if(romaChars[i] == 'C'){
                if(i < romaChars.length - 1 && romaChars[i+1] == 'D'){
                    res += 400;
                    i += 2;
                } else if (i < romaChars.length - 1 && romaChars[i+1] == 'M'){
                    res += 900;
                    i += 2;
                } else {
                    res += 100;
                    i++;
                }
            }else if(romaChars[i] == 'D'){
                res += 500;
                i++;
            }else if(romaChars[i] == 'M'){
                res += 1000;
                i++;
            }else {
                return 0;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        String roma = "MCMXCIV";
        int res = romanToInt(roma);
        System.out.println(res);
    }
}
