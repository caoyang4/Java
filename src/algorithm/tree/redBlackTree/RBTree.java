package src.algorithm.tree.redBlackTree;

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
        pr.parent = p.parent;
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

        pl.parent = p.parent;
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

    /**
     * 根据 key 删除红黑树节点
     * @param key
     * @return
     */
    public V remove(K key){
        RBNode node = getNode(key);
        if (node == null) {
            return null;
        }
        V oldValue = (V) node.value;
        deleteNode(node);
        return oldValue;
    }

    /**
     * 删除节点，并平衡调整红黑树
     * 3种情况：
     *   1、删除叶子节点，直接删除
     *   2、删除节点有一个子节点，直接用子节点替代
     *   3、删除节点有两个子节点，此时需要先找到删除节点的前驱或后继节点来替代，可以转换为两种情况
     *     3.1
     *     3.2
     * @param node
     */
    private void deleteNode(RBNode node) {
        // 情况3：有左右两个子节点
        if(leftOf(node) != null && rightOf(node) != null){
            // 找到后继节点，JDK 的 TreeMap 删除也是用后继节点替代
            RBNode successor = successor(node);
            // 用后继节点信息替换删除节点
            node.key = successor.key;
            node.value = successor.value;
            // 如此，删除节点就变为后继节点了
            node = successor;
        }
        RBNode replacement = node.left != null ? node.left : node.right;
        if (replacement != null) {
            // 情况2：有一个子节点
            replacement.parent = node.parent;
            if (node.parent == null) {
                root = replacement;
            } else if (node == leftOf(parentOf(node))){
                parentOf(node).left = replacement;
            } else {
                parentOf(node).right = replacement;
            }
            // 先将 node 节点的子节点，父节点都只为 null，再调整；等待 GC
            node.left = node.right = node.parent = null;

            // 替换完成后平衡红黑树
            if (node.color == BLACK){
                fixAfterRemove(replacement);
            }
        } else if (node.parent == null){
            // 既没有子节点，也没有父节点
            root = null;
        } else {
            // 情况1：叶子结点
            // 先调整，再删除
            if (node.color == BLACK){
                fixAfterRemove(node);
            }
            if (node == leftOf(parentOf(node))){
                parentOf(node).left = null;
            } else {
                parentOf(node).right = null;
            }
            node = null;
        }

    }

    /**
     * 删除后的调整处理
     * 2-3-4树的删除操作
     * 情况 1、自己能搞定，对应3节点或4节点
     * 情况 2、自己搞不定，向兄弟节点借，但兄弟节点不借，于是找父节点，父节点下来，兄弟节点找一个人替父节点当家
     * 情况 3、向兄弟节点借，但兄弟节点没得借
     * @param node
     */
    private void fixAfterRemove(RBNode node) {
        // 情况 2 和 3：
        while (node != root && colorOf(node) == BLACK){
            if (node == leftOf(parentOf(node))){
                RBNode broNode = rightOf(parentOf(node));
                if (colorOf(broNode) == RED){
                    // 不是真正的兄弟节点
                    setColor(broNode, BLACK);
                    setColor(parentOf(node), RED);
                    leftRotation(parentOf(node));
                    broNode = rightOf(parentOf(node));
                }
                if (colorOf(leftOf(broNode)) == BLACK && colorOf(rightOf(broNode)) == BLACK){
                    // 情况 3、向兄弟节点借，但兄弟节点没得借
                    // 先把兄弟节点设置为红色
                    setColor(broNode, RED);
                    node = parentOf(node);

                } else {
                    // 情况 2、向兄弟节点借，但兄弟节点有得借
                    if (colorOf(rightOf(broNode)) == BLACK){
                        // 不存在右子节点，必然存在左子节点
                        setColor(broNode, RED);
                        setColor(leftOf(broNode), BLACK);
                        rightRotation(broNode);
                        broNode = rightOf(parentOf(node));
                    }
                    setColor(broNode, colorOf(parentOf(node)));
                    setColor(parentOf(node), BLACK);
                    setColor(rightOf(broNode), BLACK);
                    leftRotation(parentOf(node));
                    node = root;
                }
            } else {
                // 与上述 if 分支反向
                RBNode broNode = leftOf(parentOf(node));
                if (colorOf(broNode) == RED){
                    // 不是真正的兄弟节点
                    setColor(broNode, BLACK);
                    setColor(parentOf(node), RED);
                    rightRotation(parentOf(node));
                    broNode = leftOf(parentOf(node));
                }
                if (colorOf(leftOf(broNode)) == BLACK && colorOf(rightOf(broNode)) == BLACK){
                    // 情况 3、向兄弟节点借，但兄弟节点没得借
                    // 先把兄弟节点设置为红色
                    setColor(broNode, RED);
                    node = parentOf(node);

                } else {
                    // 情况 2、向兄弟节点借，但兄弟节点有得借
                    if (colorOf(leftOf(broNode)) == BLACK){
                        // 不存在右子节点，必然存在左子节点
                        setColor(broNode, RED);
                        setColor(rightOf(broNode), BLACK);
                        leftRotation(broNode);
                        broNode = leftOf(parentOf(node));
                    }
                    setColor(broNode, colorOf(parentOf(node)));
                    setColor(parentOf(node), BLACK);
                    setColor(leftOf(broNode), BLACK);
                    rightRotation(parentOf(node));
                    node = root;
                }
            }
        }
        // 情况1：自己能搞定，对应3节点或4节点
        // 替代的节点是红色，直接置为黑色
        setColor(node, BLACK);

    }

    /**
     * 根据 key 找到红黑树节点
     * @param key
     * @return
     */
    private RBNode getNode(K key) {
        RBNode node = this.root;
        while (node != null){
            int cmp = key.compareTo((K) node.key);
            if (cmp < 0){
                node = node.left;
            } else if (cmp > 0){
                node = node.right;
            } else {
                return node;
            }
        }
        return null;
    }

    /**
     * 查找节点n的前驱节点
     * @param n
     * @return
     */
    private RBNode predecessor(RBNode n){
        if (n == null) {
            return null;
        } else if(n.left != null) {
            RBNode pre = n.left;
            while (pre.right != null){
                pre = pre.right;
            }
            return pre;
        } else {
            // 这种情况再实际删除中不会存在，因为就是叶子结点，但是再查找前驱和后继节点中是有意义的
            RBNode pre = n.parent;
            RBNode t = n;
            while (pre != null && t == pre.left){
                t = pre;
                pre = pre.parent;
            }
            return pre;
        }
    }

    /**
     * 查找节点n的后继节点
     * @param n
     * @return
     */
    private RBNode successor(RBNode n){
        if (n == null) {
            return null;
        } else if(n.right != null) {
            RBNode pre = n.right;
            while (pre.left != null){
                pre = pre.left;
            }
            return pre;
        } else {
            // 这种情况再实际删除中不会存在，因为就是叶子结点，但是再查找前驱和后继节点中是有意义的
            RBNode pre = n.parent;
            RBNode t = n;
            while (pre != null && t == pre.right){
                t = pre;
                pre = pre.parent;
            }
            return pre;
        }
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
