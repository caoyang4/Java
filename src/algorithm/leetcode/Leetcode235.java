package src.algorithm.leetcode;

/**
 * 235. 二叉搜索树的最近公共祖先
 *
 * 给定一个二叉搜索树, 找到该树中两个指定节点的最近公共祖先
 * 所有节点的值都是唯一的。
 * p、q 为不同节点且均存在于给定的二叉搜索树中
 * @author caoyang
 */
public class Leetcode235 {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        // 树到底了，或者遇到 p 或 q 直接返回
        if (root == null || p == root || q == root) {
            return root;
        }
        // 找左子树
        TreeNode left = lowestCommonAncestor(root.left, p, q);
        // 找右子树
        TreeNode right = lowestCommonAncestor(root.right, p, q);
        // 左右子树在两边
        if (left != null && right != null){
            return root;
        }
        // 取不为空的树
        return left != null ? left : right;
    }

    public static void main(String[] args) {

    }
}
