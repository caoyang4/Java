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

    public static int findByByte(int[] nums){
        int result = 0;
        for (int i = 0; i < 32; i++) {
            int count = 0;
            for (int num : nums) {
                // 计算每个位上 1 的个数
                if (((num >> i) & 1) == 1){
                    count++;
                }
            }
            // 1的数量不被3整除
            if (count % 3 != 0) {
                result |= (1 << i);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {0,1,0,1,0,1,99};
        int result = findByByte(nums);
        System.out.println(result);
    }
}
