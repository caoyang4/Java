package src.algorithm.leetcode;


/**
 * 437. 路径总和 III
 * 输入：root = [10,5,-3,3,2,null,11,3,-2,null,1], targetSum = 8
 * 输出：3
 * 解释：和等于 8 的路径有 3 条
 *
 * @author caoyang
 */
public class Leetcode437 {
    // 访问每一个节点node，检测以node为起始节点且向下延深的路径有多少种
    public int pathSum(TreeNode root, int targetSum) {
        int num = 0;
        if (root == null){return num;}
        // 根节点为起始节点搜索
        num += trackBack(root, targetSum);
        // 左节点为起始节点搜索
        num += pathSum(root.left, targetSum);
        // 右节点为起始节点搜索
        num += pathSum(root.right, targetSum);
        return num;
    }

    public int trackBack(TreeNode root, int targetSum){
        int num = 0;
        if (root == null){
            return 0;
        }
        if (targetSum == root.val){
            num++;
        }
        num += trackBack(root.left, targetSum-root.val);
        num += trackBack(root.right, targetSum-root.val);
        return num;
    }


    public static void main(String[] args) {

    }
}
