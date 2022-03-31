package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 82. 删除排序链表中的重复元素 II
 * 给定一个已排序的链表的头head，删除原始链表中所有重复数字的节点，只留下不同的数字 。返回已排序的链表
 *
 * 输入：head = [1,2,3,3,4,4,5]
 * 输出：[1,2,5]
 *
 * @author caoyang
 */
public class Leetcode82 {
    public ListNode deleteDuplicates(ListNode head) {
        ListNode cursor = head;
        ListNode tmp = null;
        Deque<ListNode> deque = new LinkedList<>();
        int before;
        while (cursor != null){
            deque.add(cursor);
            tmp = cursor;
            before = cursor.val;
            cursor = cursor.next;
            while (cursor != null && before == cursor.val){
                cursor = cursor.next;
                if (cursor == null){
                    deque.removeLast();
                }

            }
            if (cursor == null){
                break;
            } else {
                if (tmp.next != cursor) {
                    deque.removeLast();
                }
            }
        }
        ListNode result = null;
        if(!deque.isEmpty()){
           result = deque.pop();
           result.next = null;
           cursor = result;
           while (!deque.isEmpty()){
               cursor.next = deque.pop();
               cursor = cursor.next;
               if (deque.isEmpty()){
                   cursor.next = null;
               }
           }
       }
        return result;
    }

    public static void main(String[] args) {

    }
}
