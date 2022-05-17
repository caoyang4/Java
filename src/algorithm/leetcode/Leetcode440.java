package src.algorithm.leetcode;

/**
 * 440. 字典序的第K小数字
 * 给定整数 n 和 k，返回  [1, n] 中字典序第 k 小的数字
 *
 * 输入: n = 13, k = 2
 * 输出: 10
 * 解释: 字典序的排列是 [1, 10, 11, 12, 13, 2, 3, 4, 5, 6, 7, 8, 9]，所以第二小的数字是 10
 * @author caoyang
 */
public class Leetcode440 {
    public static int findKthNumber(int n, int k) {
        int num = 1;
        k--;
        while (k > 0){
            long count = trackBack(n, num, num);
            if (count <= k){ // num节点整个前缀树，数量都不够，则移到同层下一节点
                num++;
                k -= count;
            } else { // 存在于该前缀树中
                // 移到下一层
                num *= 10;
                k--;
            }
        }
        return num;
    }
    public static long trackBack(int n, long start, long end){
        if (n < start) { return 0;}
        // 当前层的节点
        long curr = Math.min(n, end) - start + 1;
        return curr + trackBack(n, start*10, end*10+9);
    }


    public static void main(String[] args) {
        int n = 804289384;
        int k = 42641503;
        int result = findKthNumber(n, k);
        System.out.println(result);
    }
}
