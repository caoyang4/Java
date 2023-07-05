package src.algorithm.leetcode;

/**
 * 42. 接雨水
 * 给定 n 个非负整数表示每个宽度为 1 的柱子的高度图，计算按此排列的柱子，下雨之后能接多少雨水
 *
 * 输入：height = [0,1,0,2,1,0,1,3,2,1,2,1]
 * 输出：6
 * @author caoyang
 */
public class Leetcode42 {
    /**
     * 按列求，找到当前柱子的左右最高柱子
     * 动态规划求左右最高柱子
     * @param height
     * @return
     */
    public static int trap(int[] height) {
        int sum = 0;
        if(height.length <= 2){
            return sum;
        }
        int len = height.length;
        int[] leftMaxHeight = new int[len];
        int[] rightMaxHeight = new int[len];
        for (int i = 1; i < len; i++) {
            leftMaxHeight[i] = Math.max(leftMaxHeight[i-1], height[i-1]);
        }
        for (int i = len-2; i >= 0; i--) {
            rightMaxHeight[i] = Math.max(rightMaxHeight[i+1], height[i+1]);
        }
        for (int i = 1; i < len-1; i++) {
            int min = Math.min(leftMaxHeight[i], rightMaxHeight[i]);
            // 较矮柱子高于当前柱子，才能盛水
            if(min > height[i]){
                sum += (min - height[i]);
            }
        }
        return sum;
    }

    /**
     * 双指针
     * @param height
     * @return
     */
    public static int trapByDoubleIndex(int[] height) {
        int len = height.length;
        int leftMax = 0;
        int rightMax = 0;
        int leftCursor = 1;
        int rightCursor = len-2;
        int sum = 0;
        for (int i = 1; i <= len-2; i++) {
            int left = height[leftCursor-1];
            int right = height[rightCursor+1];
            if(left < right){
                leftMax = Math.max(height[leftCursor-1], leftMax);
                if(leftMax > height[leftCursor]){
                    sum += (leftMax - height[leftCursor]);
                }
                leftCursor++;
            }else {
                rightMax = Math.max(height[rightCursor+1], rightMax);
                if(rightMax > height[rightCursor]){
                    sum += (rightMax - height[rightCursor]);
                }
                rightCursor--;
            }
        }
        return sum;
    }

    public static void main(String[] args) {
        int[] height = {0,1,0,2,1,0,1,3,2,1,2,1};
        int res1 = trap(height);
        int res2 = trapByDoubleIndex(height);
        System.out.println(res1);
        System.out.println(res2);
    }
}
