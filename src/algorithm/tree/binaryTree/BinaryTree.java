package src.algorithm.tree.binaryTree;

/**
 * 普通二叉树
 * @author caoyang
 */
public class BinaryTree<K extends Comparable<K>, V> {
    private Node root;

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * 二叉树节点插入
     * @param key
     * @param value
     */
    public void input(K key, V value){
        Node node = new Node(key, value);
        if(root == null){
            root = node;
        } else {
            Node n = root;
            Node parent;
            int cmp;
            do{
                parent = n;
                cmp = key.compareTo((K) n.key);
                if (cmp < 0){
                    n = n.left;
                } else if (cmp > 0){
                    n = n.right;
                } else {
                    n.setValue(value != null ? value : key);
                    return;
                }
            } while (n != null);
            if(cmp < 0){
                parent.left = node;
            } else {
                parent.right = node;
            }
            node.parent = parent;
        }
    }

    /**
     * 中序遍历
     */
    public void middleTraversal(Node root){
        if(root != null){
            middleTraversal(root.left);
            System.out.print(root + " ");
            middleTraversal(root.right);
        }
    }

    /**
     * 前序遍历
     * @param root
     */
    public void preTraversal(Node root){
        if(root != null){
            System.out.print(root + " ");
            preTraversal(root.left);
            preTraversal(root.right);
        }
    }

    /**
     * 后序遍历
     * @param root
     */
    public void postTraversal(Node root){
        if(root != null){
            postTraversal(root.left);
            postTraversal(root.right);
            System.out.print(root + " ");
        }
    }

    static class Node<K extends Comparable<K>, V>{
        private K key;
        private V value;
        private Node left;
        private Node right;
        private Node parent;

        public Node() {
        }

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }
}
