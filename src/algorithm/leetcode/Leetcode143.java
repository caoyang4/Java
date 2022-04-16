package src.algorithm.leetcode;

import java.util.*;

/**
 * 143. 重排链表
 * 给定一个单链表 L 的头节点 head ，单链表 L 表示为：
 * L0 → L1 → … → Ln-1 → Ln
 * 请将其重新排列后变为：
 * L0 → Ln → L1 → Ln-1 → L2 → Ln-2 → …
 * 不能只是单纯的改变节点内部的值，而是需要实际的进行节点交换
 *
 * @author caoyang
 */
public class Leetcode143 {
    public void reorderList1(ListNode head) {
        if(head == null || head.next == null){ return;}
        List<ListNode> list = new ArrayList<>();
        ListNode node = head;
        while (node != null){
            list.add(node);
            node = node.next;
        }
        int start = 0;
        int end = list.size() - 1;
        while (start < end) {
            list.get(start++).next = list.get(end);
            if(start == end){
                list.get(start).next = null;
                return;
            }
            list.get(end--).next = list.get(start);
        }
    }
    public void reorderList(ListNode head) {
        // 快慢指针找中间节点
        ListNode slow = head;
        ListNode fast = head;
        while (fast.next != null && fast.next.next != null){
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode left = head;
        ListNode right = slow.next;
        // 链表从中点断开
        slow.next = null;
        // 翻转右链表
        right = reverse(right);
        // 合并链表
        while (left != null && right != null){
            ListNode l = left.next;
            ListNode r = right.next;
            left.next = right;
            right.next = l;
            left = l;
            right = r;
        }
    }
    public ListNode reverse(ListNode head){
        ListNode node = null;
        while (head != null){
            ListNode tmp = head;
            head = tmp.next;
            tmp.next = node;
            node = tmp;
        }
        return node;
    }

    public static void main(String[] args) {

    }
}
