package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 109. 有序链表转换二叉搜索树
 * 给定一个单链表的头节点head，其中的元素按升序排序 ，将其转换为高度平衡的二叉搜索树
 * @author caoyang
 */
public class Leetcode109 {
    public TreeNode sortedListToBST(ListNode head) {
        if (head == null){
            return null;
        }
        List<Integer> list = new ArrayList<>();
        while (head != null){
            list.add(head.val);
            head = head.next;
        }
        return sortedListToBST(list, 0, list.size());
    }
    public TreeNode sortedListToBST(List<Integer> list, int start, int end){
        if(start == end){
            return null;
        }
        int middle = start + ((end - start) >> 1);
        TreeNode root = new TreeNode(list.get(middle));

        root.left = sortedListToBST(list,start, middle);
        root.right = sortedListToBST(list, middle+1, end);
        return root;
    }

    public static void main(String[] args) {

    }

}
