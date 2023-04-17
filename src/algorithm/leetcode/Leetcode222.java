package src.algorithm.leetcode;

/**
 * 222. 完全二叉树的节点个数
 * @author caoyang
 */
public class Leetcode222 {
    public int countNodes(TreeNode root) {
        return traverse(root);
    }
    public int traverse(TreeNode root){
        if (root == null){
            return 0;
        }
        return 1 + traverse(root.left) + traverse(root.right);

    }

    public static void main(String[] args) {

    }
}
