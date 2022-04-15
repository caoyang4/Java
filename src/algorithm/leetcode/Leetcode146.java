package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 146. LRU 缓存
 * @author caoyang
 */
public class Leetcode146 {

    static class LRUNode{
        int key;
        int value;
        LRUNode prev;
        LRUNode next;

        public LRUNode(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    static class LRUCache{
        private int capacity;
        private int size;
        LRUNode head;
        LRUNode tail;
        Map<Integer, LRUNode> map = new HashMap();

        public LRUCache(int capacity) {
            this.capacity = capacity;
        }

        public int get(int key) {
            LRUNode node = map.get(key);
            if (node == null){
                return -1;
            }
            int val = node.value;
            setHead(node);
            return val;
        }

        public void put(int key, int value) {
            LRUNode node = map.get(key);
            if (node == null){
                node = new LRUNode(key, value);
                map.put(key, node);
                size++;
            } else {
                node.value = value;
            }
            setHead(node);
            remove();
        }
        private void setHead(LRUNode node){
            if(head == node){
                return;
            }
            if (head == null){
                head = node;
                tail = node;
            } else {
                if (node.prev != null){
                    node.prev.next = node.next;
                }
                if(node.next != null){
                    node.next.prev = node.prev;
                } else {
                    tail = node.prev;
                }
                node.prev = null;
                node.next = head;
                head.prev = node;
                head = node;
            }
        }
        private void remove(){
            while (size > capacity && tail != null){
                LRUNode prev = tail.prev;
                prev.next = null;
                tail.prev = null;
                tail.next = null;
                map.remove(tail.key);
                tail = prev;
                size--;
            }
        }
    }

    public static void main(String[] args) {

    }
}
