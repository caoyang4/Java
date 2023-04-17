package src.algorithm.leetcode;

/**
 * 337. 打家劫舍 III
 * 两个直接相连的房子在同一天晚上被打劫 ，房屋将自动报警
 * 在不触动警报的情况下 ，小偷能够盗取的最高金额 。
 * @author caoyang
 */
public class Leetcode337 {
    /**
     * 不能用层序+dp，左右子树在相邻层也是无关联的
    */
    public int rob(TreeNode root) {
        int[] res = traverse(root);
        return Math.max(res[0], res[1]);
    }
    public int[] traverse(TreeNode root){
        if(root == null){
            return new int[]{0,0};
        }
        int[] leftVal = traverse(root.left);
        int[] rightVal = traverse(root.right);
        // res[0]表示偷根节点
        // res[1]表示不偷根节点
        int[] res = new int[2];
        // 偷根节点
        res[0] = leftVal[1] + rightVal[1] + root.val;
        // 不偷根节点，偷左右子节点
        res[1] =  Math.max(leftVal[0], leftVal[1]) + Math.max(rightVal[0], rightVal[1]);
        return res;
    }

    public static void main(String[] args) {

    }
}
