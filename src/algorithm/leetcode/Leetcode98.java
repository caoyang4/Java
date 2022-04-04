package src.algorithm.leetcode;

/**
 * 98. 验证二叉搜索树
 * @author caoyang
 */
public class Leetcode98 {
    public static boolean isValidBST(TreeNode root) {
        return validSubTree(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    public static boolean validSubTree(TreeNode root, long min, long max){
        if(root == null){
            return true;
        }
        if (root.val <= min || root.val >= max){
            return false;
        }
        return validSubTree(root.left, min, root.val) && validSubTree(root.right,root.val, max);
    }

    public static void main(String[] args) {

    }
}
