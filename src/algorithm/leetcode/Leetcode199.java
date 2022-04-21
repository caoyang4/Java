package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 199. 二叉树的右视图
 * @author caoyang
 */
public class Leetcode199 {
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if (root == null){ return result; }
        result = traverse(result, root);
        return result;
    }
    public List<Integer> traverse(List<Integer> result,  TreeNode root){
        if(root == null){
            return result;
        }
        result.add(root.val);
        List<Integer> leftArr = traverse(new ArrayList<>(result), root.left);
        List<Integer> rightArr = traverse(new ArrayList<>(result), root.right);
        if(leftArr.size() > rightArr.size()){
            int size = rightArr.size();
            for (int i = size; i < leftArr.size(); i++) {
                rightArr.add(leftArr.get(i));
            }
        }
        return rightArr;
    }

    public List<Integer> bfs(TreeNode root) {
        List<Integer> result = new ArrayList<>();
        if(root == null){
            return result;
        }
        Deque<TreeNode> deque = new LinkedList<>();
        deque.add(root);
        while (!deque.isEmpty()){
            int size = deque.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = deque.poll();
                if (node.left != null){
                    deque.add(node.left);
                }
                if (node.right != null){
                    deque.add(node.right);
                }
                if (i == size-1){ result.add(node.val);}
            }
        }
        return result;
    }

    public static void main(String[] args) {

    }
}
