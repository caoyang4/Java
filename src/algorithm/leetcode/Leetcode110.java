package src.algorithm.leetcode;

/**
 * 110. 平衡二叉树
 * @author caoyang
 */
public class Leetcode110 {
    public boolean isBalanced(TreeNode root) {
        if (root == null) { return true; }
        boolean rootBalanced = Math.abs(getHeight(root.left) - getHeight(root.right)) <= 1;
        return rootBalanced && isBalanced(root.left) && isBalanced(root.right);
    }
    public int getHeight(TreeNode root){
        return root == null ? 0 : Math.max(getHeight(root.left), getHeight(root.right)) + 1;
    }


    public static void main(String[] args) {

    }
}
