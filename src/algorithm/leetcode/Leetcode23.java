package src.algorithm.leetcode;

/**
 * 23. 合并K个升序链表
 * 给你一个链表数组，每个链表都已经按升序排列。
 * 请你将所有链表合并到一个升序链表中，返回合并后的链表
 *
 * 输入：lists = [[1,4,5],[1,3,4],[2,6]]
 * 输出：[1,1,2,3,4,4,5,6]
 *
 * @author caoyang
 */
public class Leetcode23 {
    public ListNode mergeKLists(ListNode[] lists) {
        if(lists == null || lists.length < 1){
            return null;
        }
        return mergeList(lists,0, lists.length-1);
    }

    public ListNode mergeList(ListNode[] lists, int start, int end){
        if(start >= end){
            return lists[start];
        }
        int middle = start + ((end - start) >> 1);
        ListNode leftLink = mergeList(lists, start, middle);
        ListNode rightLink = mergeList(lists, middle+1, end);
        return merge(leftLink, rightLink);
    }

    public ListNode merge(ListNode leftLink, ListNode rightLink){
        ListNode result = new ListNode();
        ListNode cursor = result;
        while (leftLink != null && rightLink != null){
            if(leftLink.val < rightLink.val){
                cursor.next = new ListNode(leftLink.val);
                leftLink = leftLink.next;
            } else {
                cursor.next = new ListNode(rightLink.val);
                rightLink = rightLink.next;
            }
            cursor = cursor.next;
        }
        if(leftLink != null){
            cursor.next = leftLink;
        }
        if(rightLink != null){
            cursor.next = rightLink;
        }
        return result.next;
    }

    public static void main(String[] args) {

    }
}
