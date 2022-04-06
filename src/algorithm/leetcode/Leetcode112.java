package src.algorithm.leetcode;

/**
 * 112. 路径总和
 * 给你二叉树的根节点root 和一个表示目标和的整数targetSum 。
 * 判断该树中是否存在根节点到叶子节点的路径，这条路径上所有节点值相加等于目标和targetSum 。
 * 如果存在，返回 true ；否则，返回 false
 *
 * 输入：root = [5,4,8,11,null,13,4,7,2,null,null,null,1], targetSum = 22
 * 输出：true
 *
 * @author caoyang
 */
public class Leetcode112 {
    public boolean hasPathSum(TreeNode root, int targetSum) {
        return root != null && judge(root, targetSum);
    }
    public boolean judge(TreeNode root, int targetSum){
        if(root == null){
            return targetSum == 0 ;
        }
        boolean left = judge(root.left, targetSum-root.val);
        boolean right = judge(root.right, targetSum-root.val);
        if (root.left == null){
            return right;
        } else if(root.right == null){
            return left;
        }
        return left || right;
    }

    public static void main(String[] args) {

    }
}
