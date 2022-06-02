package src.algorithm.leetcode;

/**
 * 617. 合并二叉树
 * @author caoyang
 */
public class Leetcode617 {
    public TreeNode mergeTrees(TreeNode root1, TreeNode root2) {
        if (root1 == null && root2 == null) return null;
        int val = root1 == null ? 0 : root1.val;
        val += root2 == null ? 0 : root2.val;
        TreeNode result = new TreeNode(val);
        result.left = mergeTrees(root1 == null ? null : root1.left, root2 == null ? null : root2.left);
        result.right = mergeTrees(root1 == null ? null : root1.right, root2 == null ? null : root2.right);
        return result;
    }

    public static void main(String[] args) {

    }
}
