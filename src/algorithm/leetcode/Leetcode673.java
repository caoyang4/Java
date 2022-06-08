package src.algorithm.leetcode;


/**
 * 673. 最长递增子序列的个数
 * 给定一个未排序的整数数组nums，返回最长递增子序列的个数，注意数列必须是严格递增的
 * 输入: [1,3,5,4,7]
 * 输出: 2
 * @author caoyang
 */
public class Leetcode673 {
    public static int findNumberOfLIS(int[] nums) {
        int n = nums.length;
        int[] dp = new int[n];
        int[] count = new int[n];
        int len = 0;
        for (int i = 0; i < n; i++) {
            dp[i] = count[i] = 1;
            for (int j = i-1; j >= 0; j--) {
                if(nums[j] < nums[i]){
                    // 子序列继续递增
                    if (dp[i] < dp[j]+1){
                        dp[i] = dp[j] + 1;
                        count[i] = count[j];
                    // 递增终止
                    } else if (dp[i] == dp[j] + 1){
                        count[i] += count[j];
                    }
                }
            }
            len = Math.max(len, dp[i]);
        }
        int res = 0;
        for (int i = 0; i < dp.length; i++) {
            if(dp[i] == len){
                res += count[i];
            }
        }
        return res;
    }

    public static void main(String[] args) {
        int[] nums = {1,2,4,3,5,4,7};
        int result = findNumberOfLIS(nums);
        System.out.println(result);
    }
}
