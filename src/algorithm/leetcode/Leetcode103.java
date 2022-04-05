package src.algorithm.leetcode;

import java.util.*;

/**
 * 103. 二叉树的锯齿形层序遍历
 * 给你二叉树的根节点 root ，返回其节点值的 锯齿形层序遍历 。
 * （即先从左往右，再从右往左进行下一层遍历，以此类推，层与层之间交替进行）
 *
 * 输入：root = [3,9,20,null,null,15,7]
 * 输出：[[3],[20,9],[15,7]]
 *
 * @author caoyang
 */
public class Leetcode103 {
    public List<List<Integer>> zigzagLevelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        traverse(map, root, 1);
        for (Integer level : map.keySet()) {
            if (level % 2 == 1){
                result.add(map.get(level));
            } else {
                List<Integer> tmp = new ArrayList<>();
                for (int i = map.get(level).size()-1; i >= 0; i--) {
                    tmp.add(map.get(level).get(i));
                }
                result.add(tmp);
            }
        }
        return result;
    }
    public void traverse(Map<Integer, List<Integer>> map, TreeNode root, int level){
        if (root != null){
            List<Integer> tmp = map.getOrDefault(level, new ArrayList<>());
            tmp.add(root.val);
            map.put(level, tmp);
            traverse(map, root.left, level+1);
            traverse(map, root.right, level+1);
        }
    }

    public static void main(String[] args) {

    }
}
