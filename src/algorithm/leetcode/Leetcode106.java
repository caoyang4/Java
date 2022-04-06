package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 106. 从中序与后序遍历序列构造二叉树
 * @author caoyang
 */
public class Leetcode106 {
    public TreeNode buildTree(int[] inorder, int[] postorder) {
        if (inorder == null || postorder == null){
            return null;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) {
            map.put(inorder[i], i);
        }
        int len = inorder.length;
        return buildTree(inorder, postorder, 0, len, 0, len, map);
    }
    public TreeNode buildTree(int[] inorder, int[] postorder, int iStart, int iEnd, int pStart, int pEnd, Map<Integer, Integer> map){
        if(pStart >= pEnd){
            return null;
        }
        TreeNode root = new TreeNode(postorder[pEnd-1]);
        int iRoot = map.get(postorder[pEnd-1]);
        int rightNum = iEnd - iRoot;
        root.left = buildTree(inorder, postorder, iStart, iRoot, pStart, pEnd-rightNum, map);
        root.right = buildTree(inorder, postorder, iRoot+1, iEnd, pEnd-rightNum, pEnd-1, map);
        return root;
    }


    public static void main(String[] args) {

    }
}
