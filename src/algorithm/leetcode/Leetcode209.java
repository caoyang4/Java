package src.algorithm.leetcode;


/**
 * 209. 长度最小的子数组
 * 输入：target = 7, nums = [2,3,1,2,4,3]
 * 输出：2
 * @author caoyang
 */
public class Leetcode209 {
    public static int minSubArrayLen1(int target, int[] nums) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < nums.length; i++) {
            int count = 1;
            if(nums[i] >= target){return 1;}
            int sum = nums[i];
            for (int j = i-1; j >= 0; j--) {
                count++;
                sum += nums[j];
                if (sum >= target){
                    min = Math.min(count, min);
                    break;
                }
            }

        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    /**
     * 滑动窗口
     */
    public static int minSubArrayLen(int target, int[] nums) {
        int high = 0;
        int low = 0;
        int count = Integer.MAX_VALUE;
        int sum = 0;
        while (high < nums.length){
            sum += nums[high++];
            while (sum >= target){
                count = Math.min(high-low, count);
                if(count == 1){ return count; }
                sum -= nums[low++];
            }
        }
        return count == Integer.MAX_VALUE ? 0 : count;
    }
    public static void main(String[] args) {
        int target = 7;
        int[] nums = {2,3,1,2,4,3};
        int result = minSubArrayLen(target, nums);
        System.out.println(result);
    }
}
