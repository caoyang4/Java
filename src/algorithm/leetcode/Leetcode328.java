package src.algorithm.leetcode;

/**
 * 328. 奇偶链表
 * 给定单链表的头节点head，将所有索引为奇数的节点和索引为偶数的节点分别组合在一起，然后返回重新排序的列表。
 * 第一个节点的索引被认为是奇数 ， 第二个节点的索引为偶数
 *
 * 输入: head = [1,2,3,4,5]
 * 输出: [1,3,5,2,4]
 * @author caoyang
 */
public class Leetcode328 {
    public ListNode oddEvenList(ListNode head) {
        ListNode odd = new ListNode();
        ListNode oddCursor = odd;
        ListNode even = new ListNode();
        ListNode evenCursor = even;
        int index = 0;
        while (head != null){
            if(index++ % 2 == 0){
                oddCursor.next = head;
                oddCursor = oddCursor.next;
            } else {
                evenCursor.next = head;
                evenCursor = evenCursor.next;
            }
            head = head.next;
        }
        oddCursor.next = even.next;
        evenCursor.next = null;
        return odd.next;
    }

    public static void main(String[] args) {

    }
}
