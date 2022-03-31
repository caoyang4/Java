package src.algorithm.leetcode;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 84. 柱状图中最大的矩形
 *
 * 给定 n 个非负整数，用来表示柱状图中各个柱子的高度。每个柱子彼此相邻，且宽度为 1 。
 * 求在该柱状图中，能够勾勒出来的矩形的最大面积
 *
 * 输入：heights = [2,1,5,6,2,3]
 * 输出：10
 *
 * @author caoyang
 */
public class Leetcode84 {
    /**
     * 动态规划
     * 找出每个柱子的左右最先低于当前柱子的下标
     * @param heights
     * @return
     */
    public static int largestRectangleArea(int[] heights) {
        int length = heights.length;
        int[] minLeftIndex = new int [length];
        int[] maxRigthIndex = new int [length];
        // 记录左边第一个小于该柱子的下标
        minLeftIndex[0] = -1 ;
        for (int i = 1; i < length; i++) {
            int t = i - 1;
            // 向右寻找
            while (t >= 0 && heights[t] >= heights[i]) {
                // dp
                t = minLeftIndex[t];
            }
            minLeftIndex[i] = t;
        }
        // 记录每个柱子 右边第一个小于该柱子的下标
        maxRigthIndex[length - 1] = length;
        for (int i = length - 2; i >= 0; i--) {
            int t = i + 1;
            while(t < length && heights[t] >= heights[i]) {
                // dp
                t = maxRigthIndex[t];
            }
            maxRigthIndex[i] = t;
        }
        // 求和
        int result = 0;
        for (int i = 0; i < length; i++) {
            int sum = heights[i] * (maxRigthIndex[i] - minLeftIndex[i] - 1);
            result = Math.max(sum, result);
        }
        return result;
    }

    /**
     * 单调栈
     * @param heights
     * @return
     */
    public int largestRectangleAreaByStack(int[] heights) {
        // 在柱体数组的头和尾加了两个高度为 0 的柱体。
        int[] tmp = new int[heights.length + 2];
        System.arraycopy(heights, 0, tmp, 1, heights.length);

        Deque<Integer> stack = new ArrayDeque<>();
        int area = 0;
        for (int i = 0; i < tmp.length; i++) {
            // 对栈中柱体来说，栈中的下一个柱体就是其「左边第一个小于自身的柱体」；
            // 若当前柱体 i 的高度小于栈顶柱体的高度，说明 i 是栈顶柱体的「右边第一个小于栈顶柱体的柱体」。
            // 因此以栈顶柱体为高的矩形的左右宽度边界就确定了，可以计算面积
            while (!stack.isEmpty() && tmp[i] < tmp[stack.peek()]) {
                int h = tmp[stack.pop()];
                area = Math.max(area, (i - stack.peek() - 1) * h);
            }
            stack.push(i);
        }

        return area;
    }

    public static void main(String[] args) {
        int[] heights = {2,1,5,6,2,3};
        int result = largestRectangleArea(heights);
        System.out.println(result);
    }

}
