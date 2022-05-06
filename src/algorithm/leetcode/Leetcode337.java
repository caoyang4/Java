package src.algorithm.leetcode;

/**
 * 337. 打家劫舍 III
 * 两个直接相连的房子在同一天晚上被打劫 ，房屋将自动报警
 * 在不触动警报的情况下 ，小偷能够盗取的最高金额 。
 * @author caoyang
 */
public class Leetcode337 {
    public int rob(TreeNode root) {
        if(root == null){
            return 0;
        }
        int[] leftVal = traverse(root.left);
        int[] rightVal = traverse(root.right);
        int tmp1 = leftVal[1] + rightVal[1] + root.val;
        int tmp2 = Math.max(leftVal[0], leftVal[1]) + Math.max(rightVal[0], rightVal[1]);
        return Math.max(tmp1, tmp2);
    }
    public int[] traverse(TreeNode root){
        if(root == null){
            return new int[]{0,0};
        }
        int[] leftVal = traverse(root.left);
        int[] rightVal = traverse(root.right);
        int[] res = new int[2];
        res[0] = leftVal[1] + rightVal[1] + root.val;
        res[1] =  Math.max(leftVal[0], leftVal[1]) + Math.max(rightVal[0], rightVal[1]);
        return res;
    }

    public static void main(String[] args) {

    }
}
