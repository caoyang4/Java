package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.Random;

/**
 * 384. 打乱数组
 * @author caoyang
 */
public class Leetcode384 {

    class Solution{
        private int[] nums;
        private int[] shuffleNums;
        public Solution(int[] nums) {
            this.nums = nums;
            shuffleNums = Arrays.copyOf(nums, nums.length);
        }

        public int[] reset() {
            shuffleNums = Arrays.copyOf(nums, nums.length);
            return nums;
        }

        public int[] shuffle() {
            Random random = new Random();
            for (int i = 0; i < nums.length; ++i) {
                int j = i + random.nextInt(nums.length - i);
                int temp = shuffleNums[i];
                shuffleNums[i] = shuffleNums[j];
                shuffleNums[j] = temp;
            }
            return shuffleNums;

        }
    }

    public static void main(String[] args) {

    }
}
