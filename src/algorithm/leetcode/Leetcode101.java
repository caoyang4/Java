package src.algorithm.leetcode;

/**
 * 101. 对称二叉树
 * 给你一个二叉树的根节点 root ， 检查它是否轴对称
 * @author caoyang
 */
public class Leetcode101 {
    public boolean isSymmetric(TreeNode root) {
        return isSymmetricSubTree(root.left, root.right);
    }

    public boolean isSymmetricSubTree(TreeNode p, TreeNode q){
        if(p == null && q != null){
            return false;
        } else if (p != null && q == null){
            return false;
        } else if (p != null && q != null){
            boolean left = isSymmetricSubTree(p.left, q.right);
            boolean result = p.val == q.val;
            boolean right = isSymmetricSubTree(p.right, q.left);
            return left && result && right;
        }
        return true;
    }


    public static void main(String[] args) {

    }
}
