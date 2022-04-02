package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 145. 二叉树的后序遍历
 * 左右根
 * @author caoyang
 */
public class Leetcode145 {
    public static List<Integer> postorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        traverse(root, result);
        return result;
    }
    public static void traverse(TreeNode root, List<Integer> result){
        if (root != null) {
            traverse(root.left, result);
            traverse(root.right, result);
            result.add(root.val);
        }
    }

    public static void main(String[] args) {

    }
}
