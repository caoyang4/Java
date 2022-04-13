package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 138. 复制带随机指针的链表
 *
 * @author caoyang
 */
public class Leetcode138 {
    public Node copyRandomList(Node head) {
        if (head == null){
            return null;
        }

        Node tmp = head;
        // 记录节点索引和random节点索引的映射关系
        Map<Integer, Integer> map = new HashMap<>();
        int index = 0;
        while (tmp != null){
            Node ra = tmp.random;
            int randomIndex;
            if (ra == null){
                randomIndex = Integer.MIN_VALUE;
            } else {
                Node rb = head;
                int step = 0;
                while (ra != rb && rb != null){
                    step++;
                    rb = rb.next;
                }
                randomIndex = step;
            }
            map.put(index++, randomIndex);
            tmp = tmp.next;
        }

        tmp = head;
        Node result = new Node(0);
        Node cursor = result;
        while (tmp != null){
            cursor.next = new Node(tmp.val);
            cursor = cursor.next;
            tmp = tmp.next;
        }
        cursor = result.next;
        index = 0;
        while (cursor != null){
            int step = map.get(index++);
            if (step == Integer.MAX_VALUE) {
                cursor.random = null;
            } else {
                int start = 0;
                Node r = result.next;
                while (step != start && r != null){
                    r = r.next;
                    start++;
                }
                cursor.random = r;
            }
            cursor = cursor.next;
        }
        return result.next;
    }

    public static void main(String[] args) {

    }

    static class Node {
        int val;
        Node next;
        Node random;

        public Node(int val) {
            this.val = val;
            this.next = null;
            this.random = null;
        }
    }
}
