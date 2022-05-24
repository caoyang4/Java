package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 503. 下一个更大元素 II
 * 给定一个循环数组nums（ums[nums.length - 1]的下一个元素是nums[0]），返回nums中每个元素的下一个更大元素
 * 数字x的下一个更大的元素 是按数组遍历顺序，这个数字之后的第一个比它更大的数，这意味着你应该循环地搜索它的下一个更大的数。如果不存在，则输出-1
 *
 * 输入: nums = [1,2,3,4,3]
 * 输出: [2,3,4,-1,4]
 * @author caoyang
 */
public class Leetcode503 {
    // 单调栈
    public static int[] nextGreaterElements(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        Arrays.fill(result, -1);
        // 栈中保存索引
        Deque<Integer> stack = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && nums[i] > nums[stack.peek()]){
                result[stack.pop()] = nums[i];
            }
            stack.push(i);
        }
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && nums[i] > nums[stack.peek()]){
                result[stack.pop()] = nums[i];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        int[] nums = {6,5,3,4,3};
        int[] result = nextGreaterElements(nums);
        System.out.println(Arrays.toString(result));
    }
}
