package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 230. 二叉搜索树中第K小的元素
 * 输入：root = [3,1,4,null,2], k = 1
 * 输出：1
 * @author caoyang
 */
public class Leetcode230 {
    public int kthSmallest(TreeNode root, int k) {
        List<Integer> list = new ArrayList<>();
        traverse(root, list);
        list.sort((o1,o2) -> {return o1-o2;});
        return list.get(k-1);
    }
    public void traverse(TreeNode root, List<Integer> list){
        if(root == null){
            return;
        }
        list.add(root.val);
        traverse(root.left, list);
        traverse(root.right, list);
    }

    public static void main(String[] args) {

    }

}
