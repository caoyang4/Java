package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 167. 两数之和 II - 输入有序数组
 * 给你一个下标从 1 开始的整数数组 numbers ，该数组已按非递减顺序排列  ，请你从数组中找出满足相加之和等于目标数 target 的两个数
 * 假设每个输入只对应唯一的答案 ，而且你不可以重复使用相同的元素
 * 使用常量级的额外空间
 *
 * 输入：numbers = [2,7,11,15], target = 9
 * 输出：[1,2]
 * @author caoyang
 */
public class Leetcode167 {
    public static int[] twoSum1(int[] numbers, int target) {
        int[] result = new int[2];
        int size = numbers.length;
        for (int i = 0; i < size-1; i++) {
            for (int j = i+1; j < size; j++) {
                if(numbers[i] + numbers[size-1] < target){
                    break;
                }
                if (numbers[i] + numbers[j] > target){
                    break;
                } else if (numbers[i] + numbers[j] ==  target){
                    result[0] = ++i;
                    result[1] = ++j;
                    return result;
                }
            }
        }
        return result;
    }

    public static int[] twoSum(int[] numbers, int target) {
        int[] result = new int[2];
        int start = 0;
        int end = numbers.length - 1;
        while (start < end){
            int diff = numbers[start] + numbers[end] - target;
            if (diff > 0){
                end--;
            } else if (diff < 0){
                start++;
            } else {
                result[0] = ++start;
                result[1] = ++end;
                return result;
            }
        }
        return result;
    }


    public static void main(String[] args) {
        int[] numbers = {2,7,11,15};
        int target = 9;
        int[] result = twoSum(numbers, target);
        System.out.println(Arrays.toString(result));
    }
}
