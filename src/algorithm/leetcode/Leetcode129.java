package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 129. 求根节点到叶节点数字之和
 * @author caoyang
 */
public class Leetcode129 {
    public int sumNumbers(TreeNode root) {
        if (root == null) {
            return 0;
        }
        List<List<Integer>> treePaths = treePath(root);

        int result = 0;
        for (List<Integer> digits : treePaths) {
            for (int i = 0; i < digits.size(); i++) {
                int c = digits.get(i);
                if(c == 0){ continue; }
                int num = c;
                for ( int j = digits.size()-i; j > 1; j--){
                    num *= 10;
                }
                result += num;
            }
        }
        return result;
    }
    public List<List<Integer>> treePath(TreeNode root){
        List<List<Integer>> treePaths = new ArrayList<>();
        Deque<Integer> queue = new LinkedList<>();
        trackBack(treePaths, queue, root);
        return treePaths;
    }
    public void trackBack(List<List<Integer>> treePaths, Deque<Integer> queue, TreeNode root){
        if (root == null) {
            return;
        }
        queue.add(root.val);
        if(root.left == null && root.right == null){
            treePaths.add(new ArrayList<>(queue));
            queue.removeLast();
            return;
        }
        trackBack(treePaths, queue, root.left);
        trackBack(treePaths, queue, root.right);
        queue.removeLast();
    }
    
    public static void main(String[] args) {
        
    }
}
