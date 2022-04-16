package src.algorithm.leetcode;

/**
 * 148. 排序链表
 * @author caoyang
 */
public class Leetcode148 {
    public ListNode sortList(ListNode head) {
        if (head == null || head.next == null){
            return head;
        }
        // 快慢指针找到中间节点
        ListNode slow = head;
        ListNode fast = head;
        while (fast.next != null && fast.next.next != null){
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode middle = slow.next;
        slow.next = null;
        // 归并排序
        ListNode left = sortList(head);
        ListNode right = sortList(middle);
        return merge(left, right);
    }
    public ListNode merge(ListNode left, ListNode right){
        if(left == null){
            return right;
        } else if (right == null){
            return left;
        } else {
            ListNode result = new ListNode();
            ListNode cursor = result;
            while (left != null && right != null){
                if (left.val <= right.val){
                    cursor.next = left;
                    left = left.next;
                } else {
                    cursor.next = right;
                    right = right.next;
                }
                cursor = cursor.next;
            }
            if (left != null){
                cursor.next = left;
            }
            if (right != null){
                cursor.next = right;
            }
            return result.next;
        }
    }


    public static void main(String[] args) {

    }
}
