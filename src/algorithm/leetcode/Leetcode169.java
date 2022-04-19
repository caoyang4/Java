package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 169. 多数元素
 * 给定一个大小为 n 的数组，找到其中的多数元素。多数元素是指在数组中出现次数 大于 n/2 的元素
 * 时间复杂度为 O(n)、空间复杂度为 O(1)
 *
 * 输入：[2,2,1,1,1,2,2]
 * 输出：2
 * @author caoyang
 */
public class Leetcode169 {
    /**
     *  摩尔投票法：双方拼消耗
    */
    public static int majorityElement(int[] nums) {
        int res = 0;
        int count = 0;
        for (int num : nums) {
            if (count == 0){
                res = num;
                count++;
            } else {
                count = res == num ? count+1 : count-1;
            }
        }
        return res;
    }
    public static int majorityElement1(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int num : nums) {
            int n = map.getOrDefault(num, 0);
            if (++n > (nums.length>>1)){
                return num;
            }
            map.put(num, n);
        }
        return 0;
    }

    public static void main(String[] args) {
        int[] nums = {2,2,1,1,1,2,2};
        int result = majorityElement(nums);
        System.out.println(result);
    }
}
