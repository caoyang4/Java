package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 165. 比较版本号
 *
 * 输入：version1 = "1.01", version2 = "1.001"
 * 输出：0
 * @author caoyang
 */
public class Leetcode165 {
    public static int compareVersion(String version1, String version2) {
        String[] versions1 = version1.split("\\.");
        String[] versions2 = version2.split("\\.");
        Deque<Integer> queue1 = new LinkedList<>();
        Deque<Integer> queue2 = new LinkedList<>();
        for (String version : versions1) {
            queue1.add(Integer.parseInt(version));
        }
        for (String version : versions2) {
            queue2.add(Integer.parseInt(version));
        }
        while (!queue1.isEmpty() && !queue2.isEmpty()){
            int v1 = queue1.pop();
            int v2 = queue2.pop();
            if (v1 > v2){
                return 1;
            } else if (v1 < v2){
                return -1;
            }
        }
        while (!queue1.isEmpty()){
            if (queue1.pop() != 0){ return 1; }
        }
        while (!queue2.isEmpty()){
            if (queue2.pop() != 0){ return -1; }
        }
        return 0;
    }

    public static void main(String[] args) {
        String version1 = "0.01";
        String version2 = "1.001";
        int result = compareVersion(version1, version2);
        System.out.println(result);
    }
}
