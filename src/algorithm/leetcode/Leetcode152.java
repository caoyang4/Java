package src.algorithm.leetcode;

/**
 * 152. 乘积最大子数组
 * 输入: nums = [2,3,-2,4]
 * 输出: 6
 * @author caoyang
 */
public class Leetcode152 {
    public static int maxProduct(int[] nums) {
        int product = nums[0];
        int index = 1;
        // 左右两路贪心
        for (int i = 0; i < nums.length; i++) {
            index = index != 0 ? index*nums[i] : nums[i];
            product = Math.max(product, index);
        }
        index = 1;
        for (int i =nums.length-1; i >= 0; i--) {
            index = index != 0 ? index*nums[i] : nums[i];
            product = Math.max(product, index);
        }
        return product;
    }

    public static void main(String[] args) {
        int[] nums = {-3,0,1,-2};
        int result = maxProduct(nums);
        System.out.println(result);
    }
}
