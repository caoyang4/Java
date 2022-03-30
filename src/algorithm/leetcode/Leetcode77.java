package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 77. 组合
 *
 * 给定两个整数 n 和 k，返回范围 [1, n] 中所有可能的 k 个数的组合。
 * 你可以按 任何顺序 返回答案
 *
 * 输入：n = 1, k = 1
 * 输出：[[1]]
 * @author caoyang
 */
public class Leetcode77 {
    public static List<List<Integer>> combine(int n, int k) {
        if (k > n){
            return new ArrayList<>();
        }
        int[] nums = new int[n];
        boolean[] used = new boolean[n];
        for (int i = 1; i <= n; i++) {
            nums[i-1] = i;
        }
        List<List<Integer>> result = new ArrayList<>();
        // 搜搜深度和起始点不一样
        trackBack(result, new LinkedList<>(), nums, used, k, 0, 0);
        return result;
    }
    public static void trackBack(List<List<Integer>> result, Deque<Integer> path, int[] nums, boolean[] used, int k, int depth, int start){
        if(depth == k){
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < nums.length; i++) {
            if(!used[i]){
                path.add(nums[i]);
                used[i] = true;
                trackBack(result, path, nums, used, k, depth+1, i+1);
                used[i] = false;
                path.removeLast();
            }
        }
    }

    public static void main(String[] args) {
        int n = 4;
        int k = 2;
        List<List<Integer>> result = combine(n, k);
        for (List<Integer> integers : result) {
            System.out.println(integers);
        }
    }
}
