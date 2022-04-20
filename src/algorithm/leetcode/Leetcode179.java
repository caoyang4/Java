package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 179. 最大数
 * 输入：nums = [3,30,34,5,9]
 * 输出："9534330"
 * @author caoyang
 */
public class Leetcode179 {
    public static String largestNumber(int[] nums) {
        String[] strings = new String[nums.length];
        for (int i = 0; i < nums.length; i++) {
            strings[i] = String.valueOf(nums[i]);
        }
        Arrays.sort(strings, (a, b) -> (b+a).compareTo(a+b));
        if("0".equals(strings[0])){ return "0";}
        return String.join("", strings);
    }

    public static void main(String[] args) {
        int[] nums = {111311, 1113};
        String result = largestNumber(nums);
        System.out.println(result);
    }
}
