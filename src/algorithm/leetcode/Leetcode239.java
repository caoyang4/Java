package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 239. 滑动窗口最大值
 * 给你一个整数数组 nums，有一个大小为 k 的滑动窗口从数组的最左侧移动到数组的最右侧。你只可以看到在滑动窗口内的 k 个数字。滑动窗口每次只向右移动一位
 * 输入：nums = [1,3,-1,-3,5,3,6,7], k = 3
 * 输出：[3,3,5,5,6,7]
 * @author caoyang
 */
public class Leetcode239 {
    /**
     * 遍历数组，将数组下标存放在双向队列中，并用 L,R 来标记窗口的左边界和右边界
     */
    public static int[] maxSlidingWindow(int[] nums, int k) {
        if(k == 1){return nums;}
        int size = nums.length;
        int n = size - k + 1;
        int[] window = new int[n];
        Deque<Integer> deque = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            // 如果当前遍历的数比队尾的值大，则需要弹出队尾值，直到队列重新满足从大到小的要求
            while (!deque.isEmpty() &&  nums[i] > nums[deque.peekLast()]){
                deque.removeLast();
            }
            deque.add(i);
            // 维持窗口
            // 队首的值的数组下标是否在 [L,R] 中，如果不在则需要弹出队首的值
            if(deque.peek() <= i-k){
                deque.pop();
            }
            // 当前窗口的最大值即为队首的数
            if (i+1 >= k){
                window[i+1-k] = nums[deque.peek()];
            }
        }
        return window;
    }

    /**
     * 动态规划超时
    */
    public static int[] maxSlidingWindow1(int[] nums, int k) {
        if (k == 1){return nums;}
        int size = nums.length;
        int n = size - k + 1;
        int[] dp = new int[n];
        int[] sub = new int[n];
        dp[0] = Integer.MIN_VALUE;
        sub[0] = Integer.MIN_VALUE;
        for (int i = 0; i < k; i++) {
            dp[0] = Math.max(dp[0], nums[i]);
            if(i > 0){
                sub[0] = Math.max(sub[0], nums[i]);
            }
        }
        for (int i = k; i < size; i++) {
            dp[i-k+1] = Math.max(sub[i-k], nums[i]);
            if(sub[i-k] > nums[i-k+1] || nums[i] >= nums[i-k+1]){
                sub[i-k+1] = dp[i-k+1];
            } else {
                sub[i-k+1] = nums[i-k+2];
                for (int j = i-k+3; j <= i; j++) {
                    sub[i-k+1] = Math.max( sub[i-k+1], nums[j]);
                }
            }
        }
        return dp;
    }

    public static void main(String[] args) {
        int[] nums = {1,3,1,2,0,5};
        int k = 3;
        int[] window = maxSlidingWindow(nums, k);
        System.out.println(Arrays.toString(window));
    }
}
