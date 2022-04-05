package src.algorithm.leetcode;

/**
 * 104. 二叉树的最大深度
 * @author caoyang
 */
public class Leetcode104 {
    public int maxDepth(TreeNode root) {
        return getDepth(root, 0);
    }

    public int getDepth(TreeNode root, int depth){
        if (root == null){
            return depth;
        }
        int leftDepth = getDepth(root.left, depth+1);
        int rightDepth = getDepth(root.right, depth+1);
        return Math.max(leftDepth, rightDepth);
    }

    public static void main(String[] args) {

    }

}
