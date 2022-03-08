package src.algorithm.leetcode;

/**
 * 31. 下一个排列
 * 整数数组的 下一个排列 是指其整数的下一个字典序更大的排列。
 * 如果数组的所有排列根据其字典顺序从小到大排列在一个容器中，那么数组的下一个排列就是在这个有序容器中排在它后面的那个排列。
 * 如果不存在下一个更大的排列，那么这个数组必须重排为字典序最小的排列
 *
 * 必须原地修改，只允许使用额外常数空间
 *
 * 输入：nums = [1,2,3]
 * 输出：[1,3,2]
 *
 * @author caoyang
 */
public class Leetcode31 {
    public void nextPermutation(int[] nums) {
        int len = nums.length;
        if(len <= 1){
            return;
        }
        int right = len - 1;
        // 从右遍历，先找出最大的索引k满足 nums[k] < nums[k+1]
        while (right >= 1 && nums[right] <= nums[right-1]){
            right--;
        }
        // 若找到k满足 nums[k] < nums[k+1]，即right > 0
        if(right > 0) {
            int l = len - 1;
            // 再从右找最接近且大于nums[k]的值的位置 l
            while (l >= right && nums[l] <= nums[right - 1]) {
                l--;
            }
            // 交换nums[k] 和 nums[l]
            swap(nums, right - 1, l);
        }
        // 将 right 之后的数进行翻转
        int left = right;
        right = len - 1;
        while (left < right){
            swap(nums, left++, right--);
        }
    }

    public void swap(int[] nums, int i ,int j){
        if (i == j){return;}
        nums[i] = nums[i]^nums[j];
        nums[j] = nums[i]^nums[j];
        nums[i] = nums[i]^nums[j];
    }
}
