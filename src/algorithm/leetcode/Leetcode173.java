package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 173. 二叉搜索树迭代器
 * 实现一个二叉搜索树迭代器类BSTIterator ，表示一个按中序遍历二叉搜索树（BST）的迭代器
 * @author caoyang
 */
public class Leetcode173 {

    class BSTIterator {
        TreeNode root;
        Deque<TreeNode> deque;
        public BSTIterator(TreeNode root) {
            this.root = root;
            this.deque = new LinkedList<>();
            traverse(root);
        }

        public void traverse(TreeNode root){
            if (root == null){
                return;
            }
            traverse(root.left);
            this.deque.add(root);
            traverse(root.right);
        }

        public int next() {
            return this.deque.pop().val;
        }

        public boolean hasNext() {
            return !this.deque.isEmpty();
        }
    }

    public static void main(String[] args) {

    }
}
