package src.algorithm.leetcode;

/**
 * 117. 填充每个节点的下一个右侧节点指针 II
 *
 * @author caoyang
 */
public class Leetcode117 {
    public Node connect(Node root) {
        if (root == null){
            return null;
        }
        root.next = null;
        traverse(root, root.left, root.right);
        return root;
    }
    public void traverse(Node root, Node left, Node right){
        if(left != null){
            left.next = right != null ? right : getNext(root.next);
        }
        if (right != null){
            right.next = getNext(root.next);
        }
        // 必须从右往左去连接
        // 先确保 root.right 下的节点的已完全连接，因 root.left 下的节点的连接
        // 需要 root.left.next 下的节点的信息，若 root.right 下的节点未完全连
        // 接（即先对 root.left 递归），则 root.left.next 下的信息链不完整，将
        // 返回错误的信息。可能出现的错误情况如下图所示。此时，底层最左边节点将无
        // 法获得正确的 next 信息：
        //                  o root
        //                 / \
        //     root.left  o —— o  root.right
        //               /    / \
        //              o —— o   o
        //             /        / \
        //            o        o   o
        if (right != null){
            traverse(right, right.left, right.right);
        }
        if (left != null){
            traverse(left, left.left, left.right);
        }

    }

    public Node getNext(Node root){
        if (root == null){ return null;}
        if (root.left != null){ return root.left;}
        if (root.right != null){ return root.right;}
        return getNext(root.next);
    }

    public static void main(String[] args) {

    }
}
