package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 给定一个长度为 n 的整数数组height。有n条垂线，第 i 条线的两个端点是(i, 0)和(i, height[i])。
 * 找出其中的两条线，使得它们与x轴共同构成的容器可以容纳最多的水。
 * 返回容器可以储存的最大水量。
 *
 * 输入：[1,8,6,2,5,4,8,3,7]
 * 输出：49
 * 解释：图中垂直线代表输入数组 [1,8,6,2,5,4,8,3,7]。在此情况下，容器能够容纳水（表示为蓝色部分）的最大值为49。
 *
 * @author caoyang
 */
public class Leetcode11 {

    /**
     * 双指针
     * @param height
     * @return
     */
    public static int maxArea(int[] height) {
        int left = 0;
        int right = height.length - 1;
        int leftMax = height[left];
        int rightMax = height[right];
        int mArea = Math.min(leftMax, rightMax) * (right - left);
        while (left < right) {
            if (leftMax < height[left] || rightMax < height[right]){
                leftMax = Math.max(leftMax, height[left]);
                rightMax = Math.max(rightMax, height[right]);
            } else {
                // 左值小于左最大值，直接向右移动，无需计算
                // 右值小于右最大值，直接向左移动，无需计算
                if (height[left] < height[right]){
                    // 左数小于右数，左指针向右移动
                    left++;
                } else {
                    // 左数大于右数，右指针向左移动
                    right--;
                }
                continue;
            }
            mArea = Math.max(mArea, Math.min(leftMax, rightMax) * (right - left));
        }
        return mArea;
    }

    /**
     * 垃圾暴力 O(n^2) 解法
     * @param height
     * @return
     */
    public static int maxArea1(int[] height) {
        int mArea = 0;
        for (int i = 0; i < height.length - 1; i++) {
            for (int j = i + 1; j < height.length; j++) {
                int h = Math.min(height[i], height[j]);
                mArea = Math.max(mArea, h * (j - i));
            }
        }
        return mArea;
    }

    public static void main(String[] args) {
        int[] height = {1,8,6,2,5,4,8,3,7};
        int mArea = maxArea(height);
        System.out.println(mArea);
    }
}
