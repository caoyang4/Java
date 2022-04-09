package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 124. 二叉树中的最大路径和
 * 路径 被定义为一条从树中任意节点出发，沿父节点-子节点连接，达到任意节点的序列。同一个节点在一条路径序列中 至多出现一次 。
 * 该路径至少包含一个节点，且不一定经过根节点。
 * 路径和是路径中各节点值的总和。
 *
 * 输入：root = [-10,9,20,null,null,15,7]
 * 输出：42
 *
 * @author caoyang
 */
public class Leetcode124 {
    public int max = Integer.MIN_VALUE;
    public int maxPathSum(TreeNode root) {
        getMax(root);
        return max;
    }
    public int getMax(TreeNode root) {
        if(root == null){
            return 0;
        }
        int val = root.val;
        // 如果子树路径和为负则应当置0表示最大路径不包含子树
        int left = Math.max(0, getMax(root.left));
        int right = Math.max(0, getMax(root.right));
        // 判断在该节点包含左右子树的路径和是否大于当前最大路径和
        max = Math.max(max, val + left + right);
        return Math.max(left, right)+val;
    }

    public static void main(String[] args) {
    }
}
