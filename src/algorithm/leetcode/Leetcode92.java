package src.algorithm.leetcode;

import java.util.Stack;

/**
 * 92. 反转链表 II
 * 给你单链表的头指针 head 和两个整数left 和 right ，其中left <= right 。请你反转从位置 left 到位置 right 的链表节点，返回 反转后的链表
 *
 * 输入：head = [1,2,3,4,5], left = 2, right = 4
 * 输出：[1,4,3,2,5]
 *
 * @author caoyang
 */
public class Leetcode92 {
    public ListNode reverseBetween(ListNode head, int left, int right) {
        ListNode result = new ListNode();
        ListNode cursor = result;
        Stack<ListNode> stack = new Stack<>();
        int index = 1;
        while (head != null){
            if(index < left){
                cursor.next = head;
                cursor = cursor.next;
            } else if(index <= right){
                stack.add(head);
            }
            head = head.next;
            if(index == right){
                break;
            }
            index++;
        }
        while (!stack.isEmpty()){
            cursor.next = stack.pop();
            cursor = cursor.next;
        }
        cursor.next = head;
        return result.next;
    }

    public static void main(String[] args) {

    }
}
