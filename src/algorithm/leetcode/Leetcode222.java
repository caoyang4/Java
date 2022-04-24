package src.algorithm.leetcode;

/**
 * 222. 完全二叉树的节点个数
 * @author caoyang
 */
public class Leetcode222 {
    int count = 0;
    public int countNodes(TreeNode root) {
        traverse(root);
        return count;
    }
    public void traverse(TreeNode root){
        if (root == null){
            return;
        }
        count++;
        traverse(root.left);
        traverse(root.right);
    }

    public static void main(String[] args) {

    }
}
