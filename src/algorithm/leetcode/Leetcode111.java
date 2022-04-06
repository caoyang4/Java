package src.algorithm.leetcode;

/**
 * 111. 二叉树的最小深度
 * 最小深度是从根节点到最近叶子节点的最短路径上的节点数量
 * @author caoyang
 */
public class Leetcode111 {
    public int minDepth(TreeNode root) {
        if (root == null) {
            return 0;
        }
        int left = minDepth(root.left)+1;
        int right = minDepth(root.right)+1;
        if(right == 1 || left == 1){
            return Math.max(left, right);
        } else {
            return Math.min(left, right);
        }
    }


    public static void main(String[] args) {

    }
}
