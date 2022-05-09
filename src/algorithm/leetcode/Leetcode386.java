package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 386. 字典序排数
 * @author caoyang
 */
public class Leetcode386 {
    public static List<Integer> lexicalOrder(int n) {
        List<Integer> result = new ArrayList<>();
        for (int start = 1; start <= 9; start++) {
            if (start > n){break;}
            result.add(start);
            trackBack(result, start, n, 10);
        }
        return result;
    }
    public static void trackBack(List<Integer> result, int start, int n, int multiple){
        for (int i = start*multiple; i <= start*multiple+9; i++) {
            if(i > n){return;}
            result.add(i);
            trackBack(result, i, n, multiple);
        }
    }

    public static void main(String[] args) {
        int n = 1000;
        List<Integer> result = lexicalOrder(n);
        System.out.println(result);
    }
}
