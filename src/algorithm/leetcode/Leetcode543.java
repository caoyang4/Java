package src.algorithm.leetcode;

/**
 * 543. 二叉树的直径
 * @author caoyang
 */
public class Leetcode543 {
    public int diameterOfBinaryTree(TreeNode root) {
        if (root == null) return 0;
        return trackBack(root)[0];
    }
    public int[] trackBack(TreeNode root){
        if (root == null || (root.left == null && root.right == null)){
            return new int[]{0, 0};
        }
        int[] left = trackBack(root.left);
        int[] right = trackBack(root.right);
        int r = left[1] + right[1] + (root.left != null && root.right != null ? 2 : 1);
        return new int[]{Math.max(Math.max(left[0], right[0]), r), Math.max(left[1], right[1])+1};
    }

    public static void main(String[] args) {

    }
}
