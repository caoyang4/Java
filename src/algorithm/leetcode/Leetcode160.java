package src.algorithm.leetcode;

/**
 * 160. 相交链表
 * 整个链式结构中不存在环
 * @author caoyang
 */
public class Leetcode160 {
    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        int sizeA = size(headA);
        int sizeB = size(headB);
        int diff = Math.abs(sizeA - sizeB);
        while (diff > 0){
            if (sizeA - sizeB > 0){
                headA = headA.next;
            } else {
                headB = headB.next;
            }
            diff--;
        }
        while (headA != headB && headA != null && headB != null){
            headA = headA.next;
            headB = headB.next;
        }
        return headA != null && headB != null ? headA : null;
    }
    public int size(ListNode head){
        int size = 0;
        ListNode node = head;
        while (node != null){
            size++;
            node = node.next;
        }
        return size;
    }

    public static void main(String[] args) {

    }
}
