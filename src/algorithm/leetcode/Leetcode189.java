package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 189. 轮转数组
 * 输入: nums = [1,2,3,4,5,6,7], k = 3
 * 输出: [5,6,7,1,2,3,4]
 * @author caoyang
 */
public class Leetcode189 {
    public static void rotate(int[] nums, int k) {
        int n = nums.length;
        k = k % n;
        if (k == 0){ return; }
        // 翻转所有元素 [7 6 5 4 3 2 1]
        traverse(nums, 0, n-1);
        // 翻转 [0,k-1] 区间的元素 [5 6 7 4 3 2 1]
        traverse(nums, 0, k-1);
        // 翻转 [k,n-1] 区间的元素 [5 6 7 1 2 3 4]
        traverse(nums, k, n-1);
    }
    public static void traverse(int[] nums, int start, int end){
        while (start < end){
            swap(nums, start++, end--);
        }
    }
    public static void swap(int[] nums, int i, int j){
        if (i == j){return;}
        nums[i] = nums[i] ^ nums[j];
        nums[j] = nums[i] ^ nums[j];
        nums[i] = nums[i] ^ nums[j];
    }

    public static void rotate1(int[] nums, int k) {
        int n = nums.length;
        k = k % n;
        int tmp;
        for (int i = 0; i < k; i++) {
            tmp = nums[n-1];
            for (int j = n-1; j > 0; j--) {
                nums[j] = nums[j-1];
            }
            nums[0] = tmp;
        }
    }

    public static void main(String[] args) {
        int[] nums = {1,2,3,4,5,6,7};
        int k = 3;
        rotate(nums, k);
        System.out.println(Arrays.toString(nums));
    }
}
