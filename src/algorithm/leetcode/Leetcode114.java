package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 114. 二叉树展开为链表
 * 给你二叉树的根结点 root ，请你将它展开为一个单链表：
 *  展开后的单链表应该同样使用 TreeNode ，其中 right 子指针指向链表中下一个结点，而左子指针始终为 null 。
 *  展开后的单链表应该与二叉树 先序遍历 顺序相同
 *
 * @author caoyang
 */
public class Leetcode114 {
    public void flatten(TreeNode root) {
        if (root == null) {
            return;
        }
        List<TreeNode> list = new ArrayList();
        traverse(list,root);
        TreeNode tmp = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            tmp.left = null;
            tmp.right = new TreeNode(list.get(i).val);
            tmp = tmp.right;
        }
        root = list.get(0);
    }

    public void traverse(List<TreeNode> list, TreeNode root){
        if(root == null){
            return;
        }
        list.add(root);
        traverse(list, root.left);
        traverse(list, root.right);

    }

    public static void main(String[] args) {

    }

}
