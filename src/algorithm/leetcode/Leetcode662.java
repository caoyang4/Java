package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 662. 二叉树最大宽度
 *
 * @author caoyang
 */
public class Leetcode662 {
    // BFS + 完全二叉树索引性质
    public int widthOfBinaryTree(TreeNode root) {
        if (root == null) return 0;
        int maxWidth = 0;
        Deque<TreeNode> deque = new LinkedList<>();
        // 从零开始顺序编号，则左子节点的序号：2i+1，右子节点的序号：2i+2;
        // 所以每层的宽度就可以使用：每层最后一个节点的值减去最后一个节点的值+1
        root.val = 0;
        deque.add(root);
        while (!deque.isEmpty()){
            int count = deque.size();
            int width = deque.getLast().val - deque.peekFirst().val + 1;
            for (int i = 0; i < count; i++) {
                TreeNode node = deque.pop();
                if (node.left != null){
                    deque.add(node.left);
                    node.left.val = 2 * node.val + 1;
                }
                if (node.right != null){
                    deque.add(node.right);
                    node.right.val = 2 * node.val + 2;
                }
            }
            maxWidth = Math.max(maxWidth, width);
        }
        return maxWidth;
    }

    public static void main(String[] args) {

    }
}
