package src.algorithm.leetcode;

/**
 * 701. 二叉搜索树中的插入操作
 * 输入数据保证，新值和原始二叉搜索树中的任意节点值都不同
 *
 * 二叉搜索树性质：
 *      若它的左子树不为空，则左子树上所有节点的值都小于根节点的值
 *      若它的右子树不为空，则右子树上所有节点的值都大于根节点的值
 *      它的左右子树也分别为二叉搜索树
 *
 * @author caoyang
 */
public class Leetcode701 {

    public TreeNode insertIntoBST(TreeNode root, int val) {
        if (root == null) return new TreeNode(val);
        if (root.val < val){
            root.right = insertIntoBST(root.right, val);
        } else {
            root.left = insertIntoBST(root.left, val);
        }
        return root;
    }

    public static void main(String[] args) {

    }
}
