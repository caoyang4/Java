package src.algorithm.leetcode;

import java.util.*;

/**
 * 113. 路径总和 II
 * 给你二叉树的根节点 root 和一个整数目标和 targetSum ，找出所有 从根节点到叶子节点 路径总和等于给定目标和的路径。
 * 叶子节点 是指没有子节点的节点
 *
 * 输入：root = [5,4,8,11,null,13,4,7,2,null,null,5,1], targetSum = 22
 * 输出：[[5,4,11,2],[5,8,4,5]]
 *
 * @author caoyang
 */
public class Leetcode113 {
    public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
        List<List<Integer>> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        backTrack(result, new LinkedList<>(), root, targetSum);
        return result;
    }
    public void backTrack(List<List<Integer>> result, Deque<Integer> path, TreeNode root, int targetSum){
        if(root == null){
            return;
        }
        path.add(root.val);
        if (root.left == null && root.right == null && targetSum == root.val){
            result.add(new ArrayList<>(path));
            path.removeLast();
            return;
        }
        backTrack(result, path, root.left, targetSum-root.val);
        backTrack(result, path, root.right, targetSum-root.val);
        path.removeLast();
    }

    public static void main(String[] args) {

    }
}
