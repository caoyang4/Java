package src.algorithm.leetcode;

/**
 * 450. 删除二叉搜索树中的节点
 * 给定一个二叉搜索树的根节点 root 和一个值 key，删除二叉搜索树中的key对应的节点，并保证二叉搜索树的性质不变。
 * 返回二叉搜索树（有可能被更新）的根节点的引用
 *
 * @author caoyang
 */
public class Leetcode450 {
    public TreeNode deleteNode(TreeNode root, int key) {
        if (root == null) return null;
        // 右子树
        if (key > root.val) root.right = deleteNode(root.right, key);
        // 左子树
        else if (key < root.val) root.left = deleteNode(root.left, key);
        else {
            // 叶子结点
            if (root.left == null && root.right == null) root = null;
            // 有右节点
            else if (root.right != null) {
                root.val = getSuccessor(root);
                // 删除后继节点
                root.right = deleteNode(root.right, root.val);
            }
            // 只有左节点
            else {
                root.val = getPredecessor(root);
                // 删除前驱节点
                root.left = deleteNode(root.left, root.val);
            }
        }
        return root;
    }
    public int getPredecessor(TreeNode root){
        root = root.left;
        while (root.right != null) root = root.right;
        return root.val;
    }
    public int getSuccessor(TreeNode root){
        root = root.right;
        while (root.left != null) root = root.left;
        return root.val;
    }

    public static void main(String[] args) {

    }
}
