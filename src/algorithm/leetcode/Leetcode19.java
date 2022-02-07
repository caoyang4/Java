package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 19. 删除链表的倒数第 N 个结点
 * 给定一个链表，删除链表的倒数第 n 个结点，并且返回链表的头结点。
 * @author caoyang
 */
public class Leetcode19 {
    public static void main(String[] args) {

    }

    /**
     * 快慢指针
     */
    public static ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode fast = head;
        ListNode slow = head;
        // 先让快指针走n步
        while (n-- != 0){
            fast = fast.next;
        }
        // 如果快指针走到了最后说明删除的是第一个节点,就返回head.next就好
        if(fast == null){
            return head.next;
        }
        // 使得slow每次都是在待删除的前一个节点, 所以要先让fast先走一步
        fast=fast.next;
        while (fast != null){
            fast = fast.next;
            slow = slow.next;
        }
        // 因为已经保证了是待删除节点的前一个节点, 直接删除即可
        slow.next = slow.next.next;
        return head;
    }

    /**
    垃圾解法
     */
    public static ListNode removeNthFromEnd1(ListNode head, int n) {
        if(head == null){
            return null;
        }
        ListNode cursor = new ListNode();
        ListNode res = cursor;
        List<Integer> list = new ArrayList<>();
        while(head != null) {
            list.add(head.val);
            head = head.next;
        }
        int index = 0;
        for (int v: list){
            if(index != list.size() - n){
                cursor.next = new ListNode(v);
                cursor = cursor.next;
            }
            index++;
        }
        return res.next;
    }
}





