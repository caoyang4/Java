package src.algorithm.leetcode;

/**
 * 83. 删除排序链表中的重复元素
 * 给定一个已排序的链表的头head ， 删除所有重复的元素，使每个元素只出现一次 。返回已排序的链表
 *
 * 输入：head = [1,1,2]
 * 输出：[1,2]
 * @author caoyang
 */
public class Leetcode83 {
    public ListNode deleteDuplicates(ListNode head) {
        ListNode result = new ListNode();
        ListNode cursor = result;
        int curr;
        while (head != null){
            cursor.next = head;
            cursor = cursor.next;
            curr = head.val;
            head = head.next;
            while (head != null && curr == head.val){
                head = head.next;
            }
        }
        cursor.next = null;
        return result.next;
    }

    public static void main(String[] args) {

    }
}
