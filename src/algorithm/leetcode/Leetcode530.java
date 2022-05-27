package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 530. 二叉搜索树的最小绝对差
 * @author caoyang
 */
public class Leetcode530 {
    public int getMinimumDifference(TreeNode root) {
        List<Integer> list = new ArrayList<>();
        traverse(list, root);
        int min = Integer.MAX_VALUE;
        for (int i = 1; i < list.size(); i++) {
            min = Math.min(min, list.get(i)-list.get(i-1));
        }
        return min;
    }
    public void traverse(List<Integer> list, TreeNode root){
        if (root == null) return;;
        traverse(list, root.left);
        list.add(root.val);
        traverse(list, root.right);
    }

    public static void main(String[] args) {

    }
}
