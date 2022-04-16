package src.algorithm.leetcode;

/**
 * 147. 对链表进行插入排序
 *
 * @author caoyang
 */
public class Leetcode147 {
    public ListNode insertionSortList(ListNode head) {
        ListNode node = head;
        while (node != null && node.next != null){
            if (node.val > node.next.val){
                ListNode tmp = node.next;
                node.next = node.next.next;
                tmp.next = node;
            }
        }
        return null;
    }
    public void swap(ListNode left, ListNode right){
        if (left.val > right.val){
            left.next = right.next;
        }
    }

    public static void main(String[] args) {

    }

}
