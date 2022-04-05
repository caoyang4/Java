package src.algorithm.leetcode;

/**
 * 99. 恢复二叉搜索树
 * 给你二叉搜索树的根节点 root ，该树中的恰好【两个节点】的值被错误地交换。请在不改变其结构的情况下，恢复这棵树
 *
 * 输入：root = [1,3,null,null,2]
 * 输出：[3,1,null,null,2]
 * @author caoyang
 */
public class Leetcode99 {
    TreeNode pre, t1, t2;
    public void recoverTree(TreeNode root) {
        inorder(root);
        if(t1 != null && t2 != null){
            // 交换两个节点的值
            int tmp = t1.val;
            t1.val = t2.val;
            t2.val = tmp;
        }
    }

    /**
     * 中序遍历，如果有降序对，交换即可
     */
    public void inorder(TreeNode root){
        if (root != null){
            inorder(root.left);
            if (pre != null && pre.val >= root.val){
                // 记录错误的两个节点
                if(t1 == null){
                    t1 = pre;
                }
                t2 = root;
            }
            pre = root;
            inorder(root.right);
        }

    }

    public static void main(String[] args) {

    }
}
