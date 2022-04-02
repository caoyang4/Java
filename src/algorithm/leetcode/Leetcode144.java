package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 144. 二叉树的前序遍历
 * 根左右
 * @author caoyang
 */
public class Leetcode144 {
    public static List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        traverse(root, result);
        return result;
    }
    public static void traverse(TreeNode root, List<Integer> result){
        if (root != null) {
            result.add(root.val);
            traverse(root.left, result);
            traverse(root.right, result);
        }
    }

    public static void main(String[] args) {

    }

}
