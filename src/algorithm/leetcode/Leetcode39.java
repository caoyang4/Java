package src.algorithm.leetcode;

import java.util.*;

/**
 * 39. 组合总和
 * 给你一个无重复元素的整数数组candidates和一个目标整数target，
 * 找出candidates中可以使数字和为目标数target的所有不同组合 ，并以列表形式返回。你可以按任意顺序返回这些组合。
 * candidates中的同一个数字可以无限制重复被选取 。如果至少一个数字的被选数量不同，则两种组合是不同的
 *
 * 输入：candidates = [2,3,6,7], target = 7
 * 输出：[[2,2,3],[7]]
 *
 * @author caoyang
 */
public class Leetcode39 {
    public static List<List<Integer>> combinationSum(int[] candidates, int target) {
        if(candidates == null || candidates.length == 0){
            return new ArrayList<>();
        }
        // 先排序
        Arrays.sort(candidates);
        Set<List<Integer>> res = new HashSet<>();
        trackBack(res, new ArrayList<>(), candidates, target, 0);
        List<List<Integer>> result = new ArrayList<>(res);
        return result;
    }

    public static void trackBack(Set<List<Integer>> res, List<Integer> path, int[] candidates, int target, int start){
        if (target == 0) {
            List<Integer> list = new ArrayList<>(path);
            // 排序之后去重
            list.sort((o1,o2) -> { return o1-o2;});
            res.add(new ArrayList<>(list));
            return;
        }
        for (int i = start; i < candidates.length; i++) {
            if(target < candidates[i]){
                break;
            }
            path.add(candidates[i]);
            trackBack(res, path, candidates, target-candidates[i], start);
            path.remove(path.size()-1);
        }
    }

    public static void main(String[] args) {
        int[] candidates = {1, 2, 3, 6, 7};
        int target = 7;
        List<List<Integer>> result = combinationSum(candidates, target);
        for (List<Integer> integers : result) {
            System.out.println(integers);
        }
    }
}
