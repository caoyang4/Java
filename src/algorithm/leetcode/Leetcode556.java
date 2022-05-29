package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 556. 下一个更大元素 III
 * 给你一个正整数n，请你找出符合条件的最小整数，其由重新排列n中存在的每位数字组成，并且其值大于n。
 * 如果不存在这样的正整数，则返回 -1
 * 输入：n = 12
 * 输出：21
 * @author caoyang
 */
public class Leetcode556 {
    public static int nextGreaterElement(int n) {
        char[] chars = String.valueOf(n).toCharArray();
        int size = chars.length;
        int right = size - 1;
        // 以 230254321 为例，先从右向左找到第一个不符合逆序排列的数，即 2 > 5, 不符合
        while (right > 0 && chars[right] <= chars[right-1]){
            right--;
        }
        if (right == 0){ return -1; }
        // 从右向左找最先比 2 小的数，即为 3，二者交换
        for (int i = size-1; i >= right; i--) {
            if (chars[i] > chars[right-1]){
                char c = chars[i];
                chars[i] = chars[right-1];
                chars[right-1] = c;
                break;
            }
        }
        // 将 right位置（即为 2） 之后的数，顺序排列
        Arrays.sort(chars, right, size);
        long result = Long.parseLong(String.valueOf(chars));
        return result > Integer.MAX_VALUE || n ==  result ? -1 : (int) result;
    }

    public static void main(String[] args) {
        int n = 230254321;
        int result = nextGreaterElement(n);
        System.out.println(result);
    }
}
