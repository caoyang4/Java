package src.algorithm.leetcode;

/**
 * 263. 丑数
 *
 * @author caoyang
 */
public class Leetcode263 {
    public static boolean isUgly(int n) {
        if(n == 1){return true;}
        int[] uglyNums = {2, 3, 5};
        while (n != 1){
            int t = n;
            for (int uglyNum : uglyNums) {
                if (n % uglyNum == 0){
                    n /= uglyNum;
                }
            }
            if (t == n){return false;}
        }
        return true;
    }

    public static void main(String[] args) {
        int n = 10;
        boolean result = isUgly(n);
        System.out.println(result);
    }
}
