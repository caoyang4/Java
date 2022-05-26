package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 525. 连续数组
 * 给定一个二进制数组 nums , 找到含有相同数量的 0 和 1 的最长连续子数组，并返回该子数组的长度
 * nums[i] 不是 0 就是 1
 *
 * 输入: nums = [0,1,0]
 * 输出: 2
 * @author caoyang
 */
public class Leetcode525 {
    public static int findMaxLength(int[] nums) {
        int max = 0;
        int sum = 0;
        Map<Integer, Integer> map = new HashMap<>();
        map.put(0, -1);
        for (int i = 0; i < nums.length; i++) {
            sum += nums[i] == 1 ? 1 : -1;
            if (map.containsKey(sum)){
                max = Math.max(max, i - map.get(sum));
            } else {
                map.put(sum, i);
            }
        }
        return max;
    }

    public static void main(String[] args) {
        int[] nums = {0,0,1,0,0,0,1,1};
        int result = findMaxLength(nums);
        System.out.println(result);
    }
}
