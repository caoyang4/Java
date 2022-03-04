package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 25. K个一组翻转链表
 * 给你一个链表，每k个节点一组进行翻转，请你返回翻转后的链表。
 * k是一个正整数，它的值小于或等于链表的长度。
 * 如果节点总数不是k的整数倍，那么请将最后剩余的节点保持原有顺序。
 *
 * 输入：head = [1,2,3,4,5], k = 2
 * 输出：[2,1,4,3,5]
 *
 * @author caoyang
 */
public class Leetcode25 {
    public ListNode reverseKGroup(ListNode head, int k) {
        if(head == null || k <= 1){
            return head;
        }
        // 利用栈 LIFO 完成翻转
        Deque<ListNode> deque = new LinkedList<>();
        ListNode result = new ListNode();
        ListNode tmp = result;
        ListNode tail = null;
        ListNode cursor = head;
        int i = 0;
        while (cursor != null){
            deque.push(cursor);
            cursor = cursor.next;
            i++;
            if (i == k){
                while (!deque.isEmpty()){
                    tmp.next = deque.pop();
                    tmp = tmp.next;
                }
                // 防止形成环
                tmp.next = null;
                tail = cursor;
                i = 0;
            }
        }
        tmp.next = tail;
        return result.next;
    }
}
