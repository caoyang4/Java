package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 94. 二叉树的中序遍历
 * 左根右
 * @author caoyang
 */
public class Leetcode94 {
    public static List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        traverse(root, result);
        return result;
    }
    public static void traverse(TreeNode root, List<Integer> result){
        if (root != null){
            traverse(root.left, result);
            result.add(root.val);
            traverse(root.right, result);
        }
    }

    public static void main(String[] args) {

    }
}
