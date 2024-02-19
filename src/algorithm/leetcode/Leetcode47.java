package src.algorithm.leetcode;

import java.util.*;

/**
 * 47. 全排列 II
 * 给定一个可包含重复数字的序列 nums ，按任意顺序 返回所有不重复的全排列
 *
 * 输入：nums = [1,1,2]
 * 输出：[[1,1,2], [1,2,1], [2,1,1]]
 *
 * @author caoyang
 */
public class Leetcode47 {

    // 排序 + 回溯
    public static List<List<Integer>> permuteUnique(int[] nums) {
        List<List<Integer>> result = new ArrayList<>();
        int len = nums.length;
        if (len == 0){
            return result;
        }
        Arrays.sort(nums);
        boolean[] used = new boolean[len];
        Deque<Integer> path = new LinkedList<>();
        trackBack(nums, result, 0, used, path);
        return result;
    }

    public static void trackBack(int[] nums, List<List<Integer>> result, int depth, boolean[] used, Deque<Integer> path){
        int len = nums.length;
        if(depth == len){
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < len; i++) {
            if(used[i]){
                continue;
            }
            // 去重
            if(i > 0 && nums[i] == nums[i-1] && !used[i-1]){
                continue;
            }
            path.add(nums[i]);
            used[i] = true;
            trackBack(nums, result, depth+1, used, path);
            used[i] = false;
            path.removeLast();
        }
    }


    public static void main(String[] args) {
        int[] nums = {3,3,0,3,3};
        List<List<Integer>> result = permuteUnique(nums);
        for (List<Integer> integers : result) {
            System.out.println(integers);
        }
    }
}
