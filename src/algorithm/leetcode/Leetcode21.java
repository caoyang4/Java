package src.algorithm.leetcode;

/**
 * 21. 合并两个有序链表
 * 将两个升序链表合并为一个新的升序链表并返回, 新链表是通过拼接给定的两个链表的所有节点组成的
 *
 * 输入：l1 = [1,2,4], l2 = [1,3,4]
 * 输出：[1,1,2,3,4,4]
 *
 * @author caoyang
 */
public class Leetcode21 {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        if(list1 == null){
            return list2;
        }
        if(list2 == null){
            return list1;
        }
        ListNode head1 = list1;
        ListNode head2 = list2;
        ListNode result = new ListNode();
        ListNode cursor = result;
        // 运用归并思想
        while (head1 != null && head2 != null){
            if(head1.val < head2.val){
                cursor.next = new ListNode(head1.val);
                head1 = head1.next;
            } else {
                cursor.next = new ListNode(head2.val);
                head2 = head2.next;
            }
            cursor = cursor.next;
        }
        if (head1 != null){
            cursor.next = head1;
        }
        if (head2 != null){
            cursor.next = head2;
        }
        return result.next;
    }

    public static void main(String[] args) {

    }
}
