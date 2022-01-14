package src.algorithm.lru;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * LRU，即 Least Recently Use ，直译为 “最近最少使用”
 * 链表 + HashMap 实现
 * 利用链表记录访问记录，
 * 有新数据加入时放在链表的 head 节点，每次访问也将该数据放在 head 节点，链表的 tail 一定是最早访问的节点，
 * 所以每次当容量不足的时候删除 tail 节点数据并将它的前驱节点设置为 tail
 * @author caoyang
 */
public class LruCached<K,V> {
    /**
     * map 容器
     */
    private Map<K, LruNode> map;

    /**
     * map 容量
     */
    private final int capacity;

    /**
     * 链表头部
     */
    private LruNode head;

    /**
     * 链表尾部
     */
    private LruNode tail;

    public LruCached(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
    }

    /**
     * 线程安全添加 k,v
     * @param key
     * @param value
     */
    synchronized void put(K key, V value){
        LruNode node = this.map.get(key);
        if (node != null){
            node.value = value;
            // 从现有位置拿掉，不删除该节点
            remove(node, false);
        } else {
            node = new LruNode(key, value);
            // 若超容量，先删除尾部
            if(this.map.size() >= this.capacity){
                remove(tail, true);
            }
            map.put(key, node);
        }
        // 将该节点置于头部
        setHead(node);
    }

    /**
     * 根据 key 获取 value
     * @param key
     * @return
     */
    synchronized V get(K key){
        LruNode node = this.map.get(key);
        if (node != null){
            // 将刚刚访问的节点置于链表头部
            remove(node, false);
            setHead(node);
            return node.value;
        }
        return null;
    }

    /**
     *
     * @param node
     * @param flag 是否从链表中真正删除
     */
    private void remove(LruNode node, boolean flag){
        if (node.prev != null){
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null){
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        // 将该节点从链表中先孤立取出来
        node.next = null;
        node.prev = null;

        if (flag) {
            this.map.remove(node.key);
        }
    }

    /**
     * 将节点置为头部
     * @param node
     */
    private void setHead(LruNode node){
        if (head != null) {
            node.next = head;
            head.prev = node;
        }

        head = node;

        if (tail == null){
            // 只有一个节点时，既是头也是尾
            tail = node;
        }


    }

    /**
     * 链表 node 节点
     */
    private class LruNode{
        private K key;
        private V value;

        // 前向节点
        private LruNode prev;
        // 后向节点
        private LruNode next;

        public LruNode(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public void printLru(){
        System.out.println("head:(" + head.key + "," + head.value +")");
        System.out.println("tail:(" + tail.key + "," + tail.value +")");
        System.out.println();
    }

    public static void main(String[] args) {
        LruCached lru = new LruCached(5);
        lru.put("1","a");
        lru.put("2","b");
        lru.put("3","c");
        lru.put("4","d");
        lru.put("5","e");
        System.out.println("插入 5 个元素");
        lru.printLru();
        System.out.println("插入 3 元素");
        lru.put("3","c");
        lru.printLru();
        System.out.println("插入第 6 个元素");
        lru.put("6","f");
        lru.printLru();
        System.out.println("访问 4 元素");
        lru.get("4");
        lru.printLru();
    }

}
