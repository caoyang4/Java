package src.algorithm.leetcode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 60. 排列序列
 * 给出集合[1,2,3,...,n]，其所有元素共有n! 种排列。
 * 当 n = 3 时, 所有排列如下：
 * "123"
 * "132"
 * "213"
 * "231"
 * "312"
 * "321"
 * 给定n和k，返回第k个排列
 *
 * 输入：n = 3, k = 3
 * 输出："213"
 *
 * @author caoyang
 */
public class Leetcode60 {
    public static String getPermutation(int n, int k) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i+1;
        }
        List<List<Integer>> result = new ArrayList<>();
        Deque<Integer> path =new LinkedList<>();
        boolean[] used = new boolean[n];
        trackBack(result, path, used, arr,0, n, k);
        return result.get(k-1).stream().map(String::valueOf).collect(Collectors.joining(""));
    }

    public static void trackBack(List<List<Integer>> result, Deque<Integer> path, boolean[] used, int[] arr, int depth, int n, int k){
        if(depth == n){
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < n; i++) {
            if(result.size() >= k){
                return;
            }
            if (!used[i]){
                used[i] = true;
                path.add(arr[i]);
                trackBack(result, path, used, arr, depth+1, n, k);
                used[i] = false;
                path.removeLast();
            }
        }
    }

    public static void main(String[] args) {
        int n = 4;
        int k = 9;
        String result = getPermutation(n, k);
        System.out.println(result);
    }
}
