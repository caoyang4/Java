package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 445. 两数相加 II
 * 给你两个 非空 链表来代表两个非负整数。数字最高位位于链表开始位置
 * 输入：l1 = [7,2,4,3], l2 = [5,6,4]
 * 输出：[7,8,0,7]
 * @author caoyang
 */
public class Leetcode445 {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        while (l1 != null || l2 != null){
            if(l1 != null){
                list1.add(l1.val);
                l1 = l1.next;
            }
            if(l2 != null){
                list2.add(l2.val);
                l2 = l2.next;
            }
        }
        int end1 = list1.size()-1;
        int end2 = list2.size()-1;
        List<Integer> result = new ArrayList<>();
        int carry = 0;
        while (end1 >= 0 || end2 >= 0){
            int val1 = end1 >= 0 ? list1.get(end1--) : 0;
            int val2 = end2 >= 0 ? list2.get(end2--) : 0;
            int sum = val1 + val2 + carry;
            int remain = sum % 10;
            carry = sum / 10;
            result.add(remain);
        }
        if (carry > 0){
            result.add(carry);
        }
        ListNode root = new ListNode();
        ListNode cursor = root;
        for (int i = result.size()-1; i  >= 0; i--) {
            cursor = cursor.next = new ListNode(result.get(i));
        }
        return root.next;
    }

    public static void main(String[] args) {

    }
}
