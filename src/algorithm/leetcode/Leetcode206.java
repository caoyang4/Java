package src.algorithm.leetcode;

/**
 * 206. 反转链表
 * @author caoyang
 */
public class Leetcode206 {
    public ListNode reverseList(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode slow = head;
        ListNode fast = head;
        while (fast.next != null && fast.next.next != null){
            fast = fast.next.next;
            slow = slow.next;
        }
        ListNode middle = slow.next;
        slow.next = null;
        ListNode left = reverseList(head);
        ListNode right = reverseList(middle);
        ListNode result = new ListNode();
        ListNode cursor = result;
        while (right != null){
            cursor.next = right;
            right = right.next;
            cursor = cursor.next;
        }
        cursor.next = left;
        return result.next;
    }

    public static void main(String[] args) {

    }
}
