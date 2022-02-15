package src.algorithm.tree.binaryTree;

/**
 * @author caoyang
 */
public class BinaryTreeOperation {
    public static void main(String[] args) {
        BinaryTree<Integer, Object> binaryTree = new BinaryTree<>();
        int[] array = {4, 5, 3, 7, 2, 6, 9, 8, 1,10};
        for (int i : array) {
            binaryTree.input(i, null);
        }
        System.out.println("中序遍历");
        binaryTree.middleTraversal(binaryTree.getRoot());
        System.out.println("前序遍历");
        binaryTree.preTraversal(binaryTree.getRoot());
        System.out.println("后序遍历");
        binaryTree.postTraversal(binaryTree.getRoot());
    }
}
