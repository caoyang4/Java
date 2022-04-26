package src.algorithm.leetcode;

/**
 * 226. 翻转二叉树
 * @author caoyang
 */
public class Leetcode226 {
    public TreeNode invertTree(TreeNode root) {
        reverse(root);
        return root;
    }
    public void reverse(TreeNode root){
        if (root == null){
            return;
        }
        TreeNode left = root.left;
        root.left = root.right;
        root.right = left;
        reverse(root.left);
        reverse(root.right);

    }

    public static void main(String[] args) {

    }
}
