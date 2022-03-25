package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 46. 全排列
 * 给定一个不含重复数字的数组nums，返回其所有可能的全排列
 *
 * 输入：nums = [1,2,3]
 * 输出：[[1,2,3],[1,3,2],[2,1,3],[2,3,1],[3,1,2],[3,2,1]]
 *
 * @author caoyang
 */
public class Leetcode46 {
    /**
     * 回溯算法 + DFS，分为3步
     *  1、选择
     *  2、递归
     *  3、回撤
     * @param nums
     * @return
     */
    public static List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        int len = nums.length;
        if (nums.length == 0){
            return result;
        }
        // 栈保存遍历路径
        Deque<Integer> path = new LinkedList<>();
        boolean[] used = new boolean[len];
        dfs(result, path, nums, used, 0);
        return result;
    }

    public static void dfs(List<List<Integer>> result, Deque<Integer> path, int[] nums, boolean[] used, int depth){
        // 递归终止条件
        if(depth == nums.length){
            result.add(new ArrayList<>(path));
            return;
        }
        // 1、选择
        for (int i = 0; i < nums.length; i++) {
            // 未搜索过，进行搜索
            if(!used[i]){
                // 尾部添加
                path.add(nums[i]);
                used[i] = true;
                // 2、递归
                dfs(result, path, nums, used, depth+1);
                // 3、回撤
                used[i] = false;
                // 去除最近的一个节点
                path.removeLast();
            }
        }
    }

    public static void main(String[] args) {
        int[] nums = {1,2,3};
        List<List<Integer>> result = permute(nums);
        for (List<Integer> integers : result) {
            System.out.println(integers);
        }
    }
}
