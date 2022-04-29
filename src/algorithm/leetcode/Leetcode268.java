package src.algorithm.leetcode;

/**
 * 268. 丢失的数字
 * 输入：nums = [9,6,4,2,3,5,7,0,1]
 * 输出：8
 * @author caoyang
 */
public class Leetcode268 {
    public static int missingNumber(int[] nums) {
        int n = nums.length;
        int max = Integer.MIN_VALUE;
        int sum1 = 0;
        for (int i = 0; i < n; i++) {
            max = Math.max(max, nums[i]);
            sum1 += nums[i];
        }
        if(max != n){return n;}
        int sum2 = ((n+1) * n) >> 1;
        return sum2- sum1;
    }

    public static void main(String[] args) {
        int[] nums = {9,6,4,2,3,5,7,0,1};
        int result = missingNumber(nums);
        System.out.println(result);
    }
}
