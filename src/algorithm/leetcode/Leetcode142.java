package src.algorithm.leetcode;

import java.util.HashSet;
import java.util.Set;

/**
 * 142. 环形链表 II
 * 给定一个链表的头节点  head ，返回链表开始入环的第一个节点。 如果链表无环，则返回 null
 * @author caoyang
 */
public class Leetcode142 {
    public ListNode detectCycle(ListNode head) {
        ListNode tmp = head;
        Set<ListNode> nodeSet = new HashSet<>();
        while (tmp != null){
            if(!nodeSet.contains(tmp)){
                nodeSet.add(tmp);
            } else {
                return tmp;
            }
            tmp = tmp.next;
        }
        return null;
    }

    public static void main(String[] args) {

    }
}
