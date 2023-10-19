package src.algorithm.leetcode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 78. 子集
 * 给你一个整数数组 nums ，数组中的元素互不相同 。
 * 返回该数组所有可能的子集（幂集）
 *
 * 输入：nums = [1,2,3]
 * 输出：[[],[1],[2],[1,2],[3],[1,3],[2,3],[1,2,3]]
 *
 * @author caoyang
 */
public class Leetcode78 {
    public static List<List<Integer>> subsets(int[] nums) {
        int n = nums.length;
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[n];
        for (int i = 0; i <= n; i++) {
            List<List<Integer>> iRes = new ArrayList<>();
            trackBack(iRes, new LinkedList<>(), nums, used, i, 0, 0);
            result.addAll(iRes);
        }
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
        int[] nums = {1, 2, 3, 4};
        List<List<Integer>> result = subsets(nums);
        for (List<Integer> integers : result) {
            System.out.println(integers);
        }
    }
}
