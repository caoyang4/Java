package src.algorithm.leetcode;

/**
 * 141. 环形链表
 * 给你一个链表的头节点 head ，判断链表中是否有环
 * @author caoyang
 */
public class Leetcode141 {
    /**
     * 快慢指针
     */
    public boolean hasCycle(ListNode head) {
        ListNode tmp1 = head;
        ListNode tmp2 = head;
        while (tmp1 != null && tmp2 != null){
            tmp1 = tmp1.next;
            tmp2 = tmp2.next;
            tmp2 = tmp2 == null ? null : tmp2.next;
            if(tmp1 == tmp2 && tmp1 != null){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {

    }
}
