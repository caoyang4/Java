package src.algorithm.targetOffer;

/**
 * 一个专业的小偷，计划偷窃沿街的房屋。每间房内都藏有一定的现金，影响小偷偷窃的唯一制约因素就是相邻的房屋装有相互连通的防盗系统，
 * 如果两间相邻的房屋在同一晚上被小偷闯入，系统会自动报警。
 * 给定一个代表每个房屋存放金额的非负整数数组 nums，请计算不触动警报装置的情况下 ，一夜之内能够偷窃到的最高金额。
 *
 * 输入：nums = [1,2,3,1]
 * 输出：4
 * 解释：偷窃 1 号房屋 (金额 = 1) ，然后偷窃 3 号房屋 (金额 = 3)。
 *      偷窃到的最高金额 = 1 + 3 = 4 。
 *
 * @author caoyang
 */
public class TargetOffer89 {
    /**
     * 对于第i栋房子，偷或者不偷，两者取较大者
     * 如果偷，再看第（i-2）栋房子偷或者不偷
     * 如果不偷，即看第（i-1）栋房子的情况
     * f(i) = max{f(i-1), f(i-2)+nums[i-1]}
     * @param nums
     * @return
     */
    public static int rob(int[] nums) {
        if(nums == null || nums.length == 0){
            return 0;
        }
        if(nums.length == 1){
            return nums[0];
        }
        int[] f = new int[nums.length+1];
        f[0] = 0;
        f[1] = nums[0];
        f[2] = Math.max(nums[0], nums[1]);
        for (int i = 3; i <= nums.length; i++) {
            f[i] = Math.max(f[i-1], f[i-2]+nums[i-1]);
        }
        return f[nums.length];
    }

    public static void main(String[] args) {
        int[] nums = {1,2,3,1};
        int res = rob(nums);
        System.out.println(res);
    }
}
