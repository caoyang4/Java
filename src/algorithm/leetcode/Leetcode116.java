package src.algorithm.leetcode;

/**
 * 116. 填充每个节点的下一个右侧节点指针
 * @author caoyang
 */
public class Leetcode116 {
    public Node connect(Node root) {
        if (root == null) {
            return null;
        }
        root.next = null;
        traverse(root, root.left, root.right);
        return root;
    }

    public void traverse(Node root, Node left, Node right){
        if(left == null || right == null){
            return;
        }
        left.next = right;
        right.next = root.next == null ? null : root.next.left;
        traverse(left, left.left, left.right);
        traverse(right, right.left, right.right);
    }

    public static void main(String[] args) {

    }
}
