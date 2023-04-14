package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 75. 颜色分类
 * 给定一个包含红色、白色和蓝色、共n 个元素的数组nums，原地对它们进行排序，使得相同颜色的元素相邻，并按照红色、白色、蓝色顺序排列。
 * 我们使用整数 0、1 和 2 分别表示红色、白色和蓝色
 *
 * 输入：nums = [2,0,2,1,1,0]
 * 输出：[0,0,1,1,2,2]
 *
 * @author caoyang
 */
public class Leetcode75 {
    /**
     * 计数排序
     * @param nums
     */
    public static void sortColors(int[] nums) {
        int[] colors = new int[3];
        for (int num : nums) {
            colors[num]++;
        }
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < colors[i]; j++) {
                nums[index++] = i;
            }
        }
    }

    public static void main(String[] args) {
        int[] nums = {2,0,2,1,1,0};
        sortColors(nums);
        System.out.println(Arrays.toString(nums));
    }
}
