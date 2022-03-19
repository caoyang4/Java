package src.algorithm.leetcode;

import java.util.*;

/**
 * 40. 组合总和 II
 * 给定一个候选人编号的集合candidates和一个目标数target，找出candidates中所有可以使数字和为target的组合。
 * candidates中的每个数字在每个组合中只能使用一次
 *
 * 输入: candidates=[10,1,2,7,6,1,5], target = 8,
 * 输出: [ [1,1,6], [1,2,5], [1,7], [2,6] ]
 *
 * @author caoyang
 */
public class Leetcode40 {
    /**
     * 只能使用一次的一般需要用 boolean[] 记录状态
     * @param candidates
     * @param target
     * @return
     */
    public static List<List<Integer>> combinationSum2(int[] candidates, int target) {
        if (candidates == null || candidates.length == 0){
            return new ArrayList<>();
        }
        Arrays.sort(candidates);
        List<List<Integer>> result = new ArrayList<>();
        boolean[] used = new boolean[candidates.length];
        trackBack(result, new ArrayList<>(), candidates, target, 0, used);
        return result;
    }
    public static void trackBack(List<List<Integer>> result, List<Integer> path, int[] candidates, int target, int start, boolean[] used){
        if (target == 0){
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < candidates.length; i++) {
            if(candidates[i] > target){
                break;
            }
            // 去重，且同一树层使用过的元素跳过
            if (i > 0 && candidates[i] == candidates[i - 1] && !used[i - 1]) {
                continue;
            }
            path.add(candidates[i]);
            used[i] = true;
            trackBack(result, path, candidates, target-candidates[i], i+1, used);
            path.remove(path.size()-1);
            used[i] = false;
        }
    }

    public static void main(String[] args) {
        int[] candidates = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        int target = 30;
        List<List<Integer>> result = combinationSum2(candidates, target);
        for (List<Integer> integers : result) {
            System.out.println(integers);
        }
    }
}
