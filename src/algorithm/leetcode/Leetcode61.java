package src.algorithm.leetcode;

/**
 * 61. 旋转链表
 * 给你一个链表的头节点 head ，旋转链表，将链表每个节点向右移动 k 个位置
 * 输入：head = [1,2,3,4,5], k = 2
 * 输出：[4,5,1,2,3]
 * @author caoyang
 */
public class Leetcode61 {
    public ListNode rotateRight(ListNode head, int k) {
        if(head == null){
            return null;
        }
        ListNode tmp = head;
        ListNode tail = tmp;
        int len = 0;
        while (tmp != null){
            len ++;
            tail = tmp;
            tmp = tmp.next;
        }
        int r = k % len;
        if (r == 0){
            return head;
        }
        int l = len - r;
        tmp = head;
        ListNode tmpFather = null;
        while (l > 0){
            l--;
            tmpFather = tmp;
            tmp = tmp.next;
        }
        if (tmpFather != null){
            tmpFather.next = null;
        }
        tail.next = head;
        return tmp;
    }

    public static void main(String[] args) {

    }
}
