package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 234. 回文链表
 * 输入：head = [1,2,2,1]
 * 输出：true
 * @author caoyang
 */
public class Leetcode234 {
    public boolean isPalindrome(ListNode head) {
        List<ListNode> nodes = new ArrayList<>();
        ListNode tmp = head;
        while (tmp != null){
            nodes.add(tmp);
            tmp = tmp.next;
        }
        int start = 0;
        int end = nodes.size()-1;
        while (start < end){
            if(nodes.get(start++).val != nodes.get(end--).val){
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {

    }
}
