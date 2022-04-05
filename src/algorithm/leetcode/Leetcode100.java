package src.algorithm.leetcode;

/**
 * 100. 相同的树
 * 给你两棵二叉树的根节点 p 和 q ，编写一个函数来检验这两棵树是否相同。
 * 如果两个树在结构上相同，并且节点具有相同的值，则认为它们是相同的。
 *
 * @author caoyang
 */
public class Leetcode100 {
    public boolean isSameTree(TreeNode p, TreeNode q) {
        return isSameSubTree(p, q);
    }
    public boolean isSameSubTree(TreeNode p, TreeNode q){
        if(p == null && q != null){
            return false;
        } else if (p != null && q == null){
            return false;
        } else if (p != null && q != null){
            boolean left = isSameSubTree(p.left, q.left);
            boolean result = p.val == q.val;
            boolean right = isSameSubTree(p.right, q.right);
            return left && result && right;
        }
        return true;
    }

    public static void main(String[] args) {

    }
}
