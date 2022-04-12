package src.algorithm.leetcode;

import java.util.HashMap;
import java.util.Map;

/**
 * 137. 只出现一次的数字 II
 * 输入：nums = [0,1,0,1,0,1,99]
 * 输出：99
 * @author caoyang
 */
public class Leetcode137 {
    public static int singleNumber(int[] nums) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int num : nums) {
            int times = map.getOrDefault(num, 0);
            map.put(num, ++times);
        }
        for (int num : map.keySet()) {
            if(map.get(num) == 1){
                return num;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        int[] nums = {0,1,0,1,0,1,99};
        int result = singleNumber(nums);
        System.out.println(result);
    }
}
