package src.algorithm.leetcode;

/**
 * 108. 将有序数组转换为二叉搜索树
 *
 * 给你一个整数数组nums ，其中元素已经按 升序 排列，请你将其转换为一棵高度平衡二叉搜索树。
 * 高度平衡二叉树是一棵满足每个节点的左右两个子树的高度差的绝对值不超过 1 的二叉树
 *
 * @author caoyang
 */
public class Leetcode108 {
    public TreeNode sortedArrayToBST(int[] nums) {
        if (nums == null) { return null; }
        return sortedArrayToBST(nums, 0, nums.length);
    }

    public TreeNode sortedArrayToBST(int[] nums, int start, int end){
        if(start ==  end){
            return null;
        }
        int middle = start + ((end - start) >> 1);
        TreeNode root = new TreeNode(nums[middle]);
        root.left = sortedArrayToBST(nums, start, middle);
        root.right = sortedArrayToBST(nums, middle+1, end);
        return root;
    }

    public static void main(String[] args) {

    }
}
