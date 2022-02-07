package src.algorithm.leetcode;

/**
 * 2. 两数相加
 *
 * 给你两个非空的链表，表示两个非负的整数。它们每位数字都是逆序的方式存储的，并且每个节点只能存储一位数字。
 * 请你将两个数相加，并以相同形式返回一个表示和的链表。
 *
 * @author caoyang
 */
public class Leetcode2 {
    public static ListNode addTwoNumbers(ListNode l1, ListNode l2){
        ListNode resNode = new ListNode(0);
        ListNode cursor = resNode;
        int carry = 0;
        /*
        * 链表不为空或者存在进位
        */
        while (l1 != null || l2 != null || carry != 0){
            int v1 = l1 != null ? l1.val : 0;
            int v2 = l2 != null ? l2.val : 0;
            int sum = v1 + v2 + carry;
            carry = sum / 10;
            ListNode node = new ListNode(sum % 10);
            cursor.next = node;
            // cursor游标向前移动
            cursor = node;

            if (l1 != null){ l1 = l1.next; }
            if (l2 != null){ l2 = l2.next; }
        }
        return resNode.next;
    }

    public static void printNode(ListNode l){
        while (l != null){
            System.out.print(l.val + "\t");
            l = l.next;
        }
    }

    public static void main(String[] args) {
        ListNode l1 = new ListNode(5, new ListNode(9));
        ListNode l2 = new ListNode(6);
        ListNode resNode = addTwoNumbers(l1, l2);

        printNode(resNode);

    }


}