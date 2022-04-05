package src.algorithm.leetcode;

import java.util.*;

/**
 * 102. 二叉树的层序遍历
 * 给你二叉树的根节点 root ，返回其节点值的 层序遍历 。
 * （即逐层地，从左到右访问所有节点）
 *
 * 输入：root = [3,9,20,null,null,15,7]
 * 输出：[[3],[9,20],[15,7]]
 * @author caoyang
 */
public class Leetcode102 {
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        traverse(map, root, 1);
        for (Integer level : map.keySet()) {
            result.add(map.get(level));
        }
        return result;
    }
    public void traverse(Map<Integer, List<Integer>> map, TreeNode root, int level){
        if (root != null){
            List<Integer> tmp = map.getOrDefault(level, map.getOrDefault(level, new ArrayList<>()));
            tmp.add(root.val);
            map.put(level, tmp);
            traverse(map, root.left, level+1);
            traverse(map, root.right, level+1);
        }
    }


    public static void main(String[] args) {

    }
}
