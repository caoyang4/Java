package src.algorithm.leetcode;

import java.util.List;

/**
 * 24. 两两交换链表中的节点
 * 给你一个链表，两两交换其中相邻的节点，并返回交换后链表的头节点
 * 在不修改节点内部的值，只能进行节点交换
 *
 * 输入：head = [1,2,3,4]
 * 输出：[2,1,4,3]
 *
 * @author caoyang
 */
public class Leetcode24 {
    public ListNode swapPairs(ListNode head) {
        if(head == null){
            return null;
        }
        ListNode cursor = head;
        ListNode link = null;
        while (head != null && head.next != null){
            ListNode tmp1 = head;
            ListNode tmp2 = head.next;
            tmp1.next = tmp2.next;
            tmp2.next = tmp1;
            head = tmp2;
            if (link == null) {
                cursor = head;
            } else {
                link.next = head;
            }
            link = head.next;
            head = head.next.next;
        }
        if (head != null && link != null){
            link.next = head;
        }
        return cursor;
    }

    public static void main(String[] args) {

    }
}
