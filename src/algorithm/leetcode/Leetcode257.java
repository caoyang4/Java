package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 257. 二叉树的所有路径
 * 给你一个二叉树的根节点 root ，按任意顺序 ，返回所有从根节点到叶子节点的路径
 *
 * 输入：root = [1,2,3,null,5]
 * 输出：["1->2->5","1->3"]
 * @author caoyang
 */
public class Leetcode257 {
    public List<String> binaryTreePaths(TreeNode root) {
        List<String> result = new ArrayList<>();
        traverse(root, result, new LinkedList<>());
        return result;
    }
    public void traverse(TreeNode root, List<String> result, Deque<String> path){
        if (root == null){return;}
        path.add(String.valueOf(root.val));
        if (root.left != null){
            traverse(root.left, result, path);
            path.removeLast();
        }
        if (root.right != null){
            traverse(root.right, result, path);
            path.removeLast();
        }
        if(root.left == null && root.right == null){
            result.add(String.join("->", path));
        }
    }

    public static void main(String[] args) {

    }
}
