package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 216. 组合总和 III
 * 找出所有相加之和为 n 的 k 个数的组合，且满足下列条件：
 * 只使用数字1到9
 * 每个数字 最多使用一次
 * 输入: k = 3, n = 7
 * 输出: [[1,2,4]]
 * @author caoyang
 */
public class Leetcode216 {
    public static List<List<Integer>> combinationSum3(int k, int n) {
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[10];
        trackBack(n, result, new LinkedList<>(), used, k, 1);
        return result;
    }
    public static void trackBack(int target, List<List<Integer>> result, Deque<Integer> path, boolean[] used, int k, int start){
        if (path.size() == k){
            if (target == 0){
                result.add(new ArrayList<>(path));
            }
            return;
        }
        for (int i = start; i <= 9; i++) {
            if (!used[start] && target-start >= 0){
                used[i] = true;
                path.add(i);
                trackBack(target-i, result, path, used, k, i+1);
                path.removeLast();
                used[i] = false;
            }
        }
    }


    public static void main(String[] args) {
        int k = 3;
        int n = 7;
        List<List<Integer>> result = combinationSum3(k, n);
        for (List<Integer> integers : result) {
            System.out.println(integers);
        }
    }
}
