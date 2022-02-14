package src.algorithm.redBlackTree;

/**
 * 红黑树
 * @author caoyang
 */
public class RBTree<K extends Comparable<K>, V> {
    public static final boolean RED = false;
    public static final boolean BLACK = true;
    private RBNode root;

    public RBNode getRoot() {
        return root;
    }

    public void setRoot(RBNode root) {
        this.root = root;
    }

    /**
     * 节点 p 左旋
     *        p              pr
     *       /\             /\
     *     pl  pr  -->     p  rr
     *         /\         /\
     *       rl  rr     pl  rl
     * 判断：p 是否存在父节点
     *   是：pr.parent = p.parent
     *   否：pr = root
     *
     * p -> pl 和 pr -> rr 不变
     * pr -> rl 变为 p -> rl
     * p -> pr  变为 pr -> p
     *
     *
     * @param p
     */
    public void leftRotation(RBNode p){
        if (p == null) {
            return;
        }
        RBNode pr = p.right;
        // pr -> rl 变为 p -> rl
        p.right = pr.left;
        if (pr.left != null) {
            pr.left.parent = p;
        }

        // p 是否存在父节点
        if(p.parent == null){
            root = pr;
        } else if(p.parent.left == p){
            p.parent.left = pr;
        } else {
            p.parent.right = pr;
        }

        // p -> pr  变为 pr -> p
        pr.left = p;
        p.parent = pr;
    }

    /**
     * 节点 p 右旋
     *         p             pl
     *        /\            /\
     *      pl  pr   -->  ll  p
     *      /\               /\
     *    ll  lr           lr  pr
     * @param p
     */
    public void rightRotation(RBNode p){
        if (p == null) {
            return;
        }

        RBNode pl = p.left;
        p.left = pl.right;
        if(pl.right != null){
            pl.right.parent = p;
        }

        if (p.parent == null) {
            root = pl;
        } else if (p.parent.left == p){
            p.parent.left = pl;
        } else {
            p.parent.right = pl;
        }

        pl.right = p;
        p.parent = pl;
    }

    /**
     * 红黑树节点的插入
     *  1、普通二叉树节点插入
     *  2、红黑树的平衡（旋转+变色）
     *
     * @param key
     * @param value
     */
    public void put(K key, V value){

        if (key == null) {
            throw new NullPointerException();
        }
        RBNode t = this.root;
        if (t == null) {
            root = new RBNode(key, value!=null?value:key, null);
            setColor(root, BLACK);
            return;
        }
        int cmp;
        RBNode parent;

        do{
            parent = t;
            cmp = key.compareTo((K) t.key);
            if(cmp < 0){
                t = t.left;
            }else if (cmp > 0){
                t = t.right;
            }else {
                t.setValue(value!=null?value:key);
                return;
            }
        } while (t != null);

        // 定位节点的插入位置
        RBNode e = new RBNode(key, value!=null?value:key, parent);
        if(cmp < 0){
            parent.left = e;
        }else {
            parent.right = e;
        }

        // 对插入节点进行旋转变色，红黑树平衡处理
        fixAfterPut(e);

    }

    /**
     * 红黑树平衡处理(旋转+变色)
     * 1. 2-3-4树 2节点 新增一个元素，直接合并为一个 3 节点
     *      红黑树：新增一个红色节点，该红色节点会添加在黑色节点下：不需要调整
     * 2. 2-3-3 树 3 节点 新增一个元素，合并为一个 4 节点
     *      红黑树：存在 6 种情况：
     *            两种（左中右），不需要处理
     *            根左左，根右右 旋转一次
     *            根左右，根右左 旋转两次
     * 3. 2-3-4 树 4 节点 新增一个元素，4 节点的中间元素升级为父节点，新增元素和剩下节点合并
     *      红黑树：新增节点是红色+爷爷节点是黑色，父节点和叔叔节点是红色，调整为：
     *             爷爷节点变为红色，父节点和叔叔节点变为黑色，若爷爷节点为 root，调整为黑色
     *
     * @param x
     */
    private void fixAfterPut(RBNode x) {
        // 插入的节点都是红色节点
        setColor(x, RED);
        while(x != null && x != root && parentOf(x).color == RED){
            // 1. x的父节点是 x爷爷节点的左节点，有 4 种情况处理
            if (parentOf(x) == parentOf(parentOf(x)).left){
                // 在该种四种条件下，根据是否有叔叔节点分为两种情况，即x爷爷节点的右节点是否存在
                RBNode y = rightOf(parentOf(parentOf(x)));
                // 存在叔叔节点
                if (colorOf(y) == RED){
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    // 递归处理
                    x = parentOf(parentOf(x));
                } else {
                    if(x == rightOf(parentOf(x))){
                        // 插入节点是父节点的右节点，需要对父节点进行一次左旋
                        x = parentOf(x);
                        leftRotation(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    // 对爷爷节点进行一次右旋
                    rightRotation(parentOf(parentOf(x)));
                }

            }
            // 2. x的父节点是 x爷爷节点的右节点，有 4 种情况处理（与上述四种情况相反）
            else {
                RBNode y = leftOf(parentOf(parentOf(x)));
                // 存在叔叔节点
                if (colorOf(y) == RED){
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    // 递归处理
                    x = parentOf(parentOf(x));
                } else {
                    if(x == leftOf(parentOf(x))){
                        // 插入节点是父节点的右节点，需要对父节点进行一次右旋
                        x = parentOf(x);
                        rightRotation(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    // 对爷爷节点进行一次左旋
                    leftRotation(parentOf(parentOf(x)));
                }

            }
        }
        // root 节点是黑色节点
        setColor(root, BLACK);

    }

    private boolean colorOf(RBNode n){
        return n == null ? BLACK : n.color;
    }
    private RBNode parentOf(RBNode n){
        return n == null ? null : n.parent;
    }
    private RBNode leftOf(RBNode n){
        return n == null ? null : n.left;
    }
    private RBNode rightOf(RBNode n){
        return n == null ? null : n.right;
    }
    private void setColor(RBNode n, boolean color){
        if (n != null) {
            n.setColor(color);
        }
    }

    /**
     * 节点类
     * @param <K>
     * @param <V>
     */
    static class RBNode<K extends Comparable<K>, V>{
        /**
         * 父节点
         */
        private RBNode parent;
        /**
         * 左子节点
         */
        private RBNode left;
        /**
         * 右子节点
         */
        private RBNode right;

        private K key;
        private V value;

        /**
         * 节点颜色
         */
        private boolean color;

        public RBNode() {
        }

        public RBNode(K key, V value, RBNode parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        public RBNode(RBNode parent, RBNode left, RBNode right, K key, V value, boolean color) {
            this.parent = parent;
            this.left = left;
            this.right = right;
            this.key = key;
            this.value = value;
            this.color = color;
        }

        public RBNode getParent() {
            return parent;
        }

        public void setParent(RBNode parent) {
            this.parent = parent;
        }

        public RBNode getLeft() {
            return left;
        }

        public void setLeft(RBNode left) {
            this.left = left;
        }

        public RBNode getRight() {
            return right;
        }

        public void setRight(RBNode right) {
            this.right = right;
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

        public boolean isColor() {
            return color;
        }

        public void setColor(boolean color) {
            this.color = color;
        }
    }
}
