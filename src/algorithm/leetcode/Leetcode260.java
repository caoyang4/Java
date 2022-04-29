package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 260. 只出现一次的数字 III
 * 输入：nums = [1,2,1,3,2,5]
 * 输出：[3,5]
 * @author caoyang
 */
public class Leetcode260 {
    public static int[] singleNumber(int[] nums) {
        int bit = 0;
        for (int i = 0; i < nums.length; i++) {
            bit ^= nums[i];
        }
        // 取异或值最后一个二进制位为 1 的数字作为 mask, 如果是 1 则表示两个数字在这一位上不同
        bit &= (-bit);
        int[] ans = new int[2];
        for (int num : nums) {
            if((num & bit) == 0){
                ans[0] ^= num;
            } else {
                ans[1] ^= num;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        int[] nums = {1,2,1,3,2,5};
        int[] result = singleNumber(nums);
        System.out.println(Arrays.toString(result));
    }
}
