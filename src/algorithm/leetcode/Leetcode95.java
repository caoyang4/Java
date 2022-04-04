package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 95. 不同的二叉搜索树 II
 * 给你一个整数 n ，请你生成并返回所有由 n 个节点组成且节点值从 1 到 n 互不相同的不同 二叉搜索树
 * @author caoyang
 */
public class Leetcode95 {
    public static List<TreeNode> generateTrees(int n) {
        return mergeTree(1, n);
    }
    public static List<TreeNode> mergeTree(int start, int end){
        List<TreeNode> result = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            List<TreeNode> left = mergeTree(start, i-1);
            List<TreeNode> right = mergeTree(i+1, end);
            for (TreeNode leftNode : left) {
                for (TreeNode rightNode : right) {
                    // 左子树值 < i
                    // 右子树值 > i
                    result.add(new TreeNode(i, leftNode, rightNode));
                }
            }
        }
        if (result.isEmpty()){
            result.add(null);
        }
        return result;
    }

    /**
     * 构建平衡二叉树
     */
    public static TreeNode generateBalancedTree(int start, int end){
        if (start > end){
            return null;
        }
        int middle = start + ((end - start) >> 1);
        // 对于中间节点，构建左右子树
        return new TreeNode(middle, generateBalancedTree(start, middle), generateBalancedTree(middle+1, end));
    }

    public static void main(String[] args) {

    }
}
