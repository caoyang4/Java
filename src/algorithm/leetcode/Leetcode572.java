package src.algorithm.leetcode;

/**
 * 572. 另一棵树的子树
 * @author caoyang
 */
public class Leetcode572 {
    public boolean isSubtree(TreeNode root, TreeNode subRoot) {
        if (root == null) return false;
        return isSameTree(root, subRoot) || isSubtree(root.left, subRoot) || isSubtree(root.right, subRoot);
    }
    public boolean isSameTree(TreeNode node, TreeNode subRoot){
        if ((node == null && subRoot != null) || (node != null && subRoot == null)) return false;
        if (node == null) return true;
        if (node.val != subRoot.val){
            return false;
        } else {
            return isSameTree(node.left, subRoot.left) && isSameTree(node.right, subRoot.right);
        }
    }

    public static void main(String[] args) {

    }
}
