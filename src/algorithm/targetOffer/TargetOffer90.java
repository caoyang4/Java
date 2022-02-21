package src.algorithm.targetOffer;

import java.util.Arrays;

/**
 * 一个专业的小偷，计划偷窃一个环形街道上沿街的房屋，每间房内都藏有一定的现金。这个地方所有的房屋都围成一圈 ，这意味着第一个房屋和最后一个房屋是紧挨着的。
 * 同时，相邻的房屋装有相互连通的防盗系统，如果两间相邻的房屋在同一晚上被小偷闯入，系统会自动报警 。
 * 给定一个代表每个房屋存放金额的非负整数数组 nums ，请计算在不触动警报装置的情况下 ，今晚能够偷窃到的最高金额。
 *
 * 输入：nums = [2,3,2]
 * 输出：3
 * 解释：你不能先偷窃 1 号房屋（金额 = 2），然后偷窃 3 号房屋（金额 = 2）, 因为他们是相邻的。
 *
 * @author caoyang
 */
public class TargetOffer90 {
    /**
     *  若没偷第 0 栋房子，则可偷第 i-1 栋房子，即 nums1 = nums[1:n-1]
     *  若偷第 0 栋房子，则不可偷第 i-1 栋房子，即 nums1 = nums[0:n-2]
     *  两者取较大者
      */
    public static int rob(int[] nums) {
        if(nums == null || nums.length == 0){ return 0; }
        if (nums.length == 1){ return nums[0]; }
        return Math.max(subRob(Arrays.copyOfRange(nums, 0, nums.length - 1)), subRob(Arrays.copyOfRange(nums, 1, nums.length)));
    }
    public static int subRob(int[] subNum){
        if(subNum.length == 1){ return subNum[0]; }
        int[] f = new int[subNum.length + 1];
        f[0] = 0;
        f[1] = subNum[0];
        for (int i = 2; i <= subNum.length; i++) {
            f[i] = Math.max(f[i-1], f[i-2] + subNum[i-1]);
        }
        return f[subNum.length];
    }

    public static void main(String[] args) {
        int[] nums = {2,3,2,4,4};
        int res = rob(nums);
        System.out.println(res);

    }

}
