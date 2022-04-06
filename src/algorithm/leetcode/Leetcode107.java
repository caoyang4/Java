package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 107. 二叉树的层序遍历 II
 * 给你二叉树的根节点 root ，返回其节点值 自底向上的层序遍历
 * 即按从叶子节点所在层到根节点所在的层，逐层从左向右遍历
 *
 * 输入：root = [3,9,20,null,null,15,7]
 * 输出：[[15,7],[9,20],[3]]
 *
 * @author caoyang
 */
public class Leetcode107 {
    public List<List<Integer>> levelOrderBottom(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        traverse(map, root, 0);
        for (Integer level : map.keySet()) {
            result.add(0, map.get(level));
        }
        return result;
    }
    public void traverse(Map<Integer, List<Integer>> map, TreeNode root, int level){
        if (root == null){
            return;
        }
        List<Integer> tmp = map.getOrDefault(level, new ArrayList<>());
        tmp.add(root.val);
        map.put(level, tmp);
        traverse(map, root.left, level+1);
        traverse(map, root.right, level+1);
    }

    public static void main(String[] args) {

    }

}
