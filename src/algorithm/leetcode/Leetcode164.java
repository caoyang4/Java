package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 164. 最大间距
 * 输入: nums = [3,6,9,1]
 * 输出: 3
 * 解释: 排序后的数组是 [1,3,6,9], 其中相邻元素 (3,6) 和 (6,9) 之间都存在最大差值 3
 *
 * @author caoyang
 */
public class Leetcode164 {
    public static int maximumGap(int[] nums) {
        int max = Integer.MIN_VALUE;
        for (int val : nums) {
            max = Math.max(max, val);
        }
        int pow = 0;
        while (max > 0){
            max /= 10;
            pow++;
        }
        // 数字只有 0-9，故数组长度为10即可
        int[] count = new int[10];
        int[] result = new int[nums.length];
        for (int i = 0; i < pow; i++) {
            int division = (int) Math.pow(10, i);
            for (int ele : nums) {
                int num = ele / division % 10;
                count[num]++;
            }
            // 计数排序
            for (int j = 1; j < count.length; j++) {
                count[j] += count[j-1];
            }
            for (int j = nums.length-1; j >= 0; j--) {
                int num = nums[j] / division % 10;
                result[--count[num]] = nums[j];
            }

            System.arraycopy(result, 0, nums, 0, nums.length);
            // 将 count 重置为 0 数组
            Arrays.fill(count, 0);
        }

        int maxGap = Integer.MIN_VALUE;
        for (int i = 1; i < nums.length; i++) {
            maxGap = Math.max(maxGap, nums[i]-nums[i-1]);
        }

        return maxGap == Integer.MIN_VALUE ? 0 : maxGap;
    }

    public static void main(String[] args) {
        int[] nums = {3,6,9,1};
        int reuslt = maximumGap(nums);
        System.out.println(reuslt);
    }
}
