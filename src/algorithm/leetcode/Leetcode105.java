package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 105. 从前序与中序遍历序列构造二叉树
 *
 * 输入: preorder = [3,9,20,15,7], inorder = [9,3,15,20,7]
 * 输出: [3,9,20,null,null,15,7]
 *
 * @author caoyang
 */
public class Leetcode105 {
    /**
     * 重建二叉树的基本思路就是先构造根节点，再构造左子树，接下来构造右子树
     * 构造左子树和右子树是一个子问题，递归处理即可。
     * 因此我们只关心如何构造根节点，以及如何递归构造左子树和右子树。
     */
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        if(preorder == null || inorder == null){
            return null;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < inorder.length; i++) {
            map.put(inorder[i], i);
        }
        int len = preorder.length;
        return doBuildTree(preorder, inorder, 0, len, 0, len, map);
    }

    public TreeNode doBuildTree(int[] preorder, int[] inorder, int pStart, int pEnd, int iStart, int iEnd, Map<Integer, Integer> map){
        if (pStart >= pEnd){
            return null;
        }
        TreeNode root = new TreeNode(preorder[pStart]);
        int iRoot = map.get(preorder[pStart]);
        int leftNum = iRoot - iStart + 1;
        root.left = doBuildTree(preorder, inorder, pStart+1, pStart+leftNum,  iStart, iRoot, map);
        root.right = doBuildTree(preorder, inorder, pStart+leftNum, pEnd, iRoot+1, iEnd, map);
        return root;
    }

    public static void main(String[] args) {

    }
}
