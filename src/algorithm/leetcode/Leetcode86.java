package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 86. 分隔链表
 * 给你一个链表的头节点 head 和一个特定值 x ，请你对链表进行分隔，使得所有小于 x 的节点都出现在 大于或等于 x 的节点之前。
 * 你应当 保留 两个分区中每个节点的初始相对位置
 *
 * 输入：head = [1,4,3,2,5,2], x = 3
 * 输出：[1,2,2,4,3,5]
 *
 * @author caoyang
 */
public class Leetcode86 {
    public ListNode partition(ListNode head, int x) {
        ListNode result = new ListNode();
        Deque<ListNode> smallNodes = new LinkedList<>();
        Deque<ListNode> bigNodes = new LinkedList<>();
        ListNode tmp = head;
        while (tmp != null){
            if (tmp.val < x){
                smallNodes.add(tmp);
            } else {
                bigNodes.add(tmp);
            }
            tmp = tmp.next;
        }
        // 有一个为空栈，说明不需要调换
        if(smallNodes.isEmpty()|| bigNodes.isEmpty()){
            return head;
        }
        tmp = result;
        while (!smallNodes.isEmpty()) {
            tmp.next = smallNodes.pop();
            tmp = tmp.next;
        }
        while (!bigNodes.isEmpty()){
            tmp.next = bigNodes.pop();
            tmp = tmp.next;
        }
        tmp.next = null;
        return result.next;
    }

    public static void main(String[] args) {

    }
}
