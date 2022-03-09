package src.algorithm.leetcode;

/**
 * 33. 搜索旋转排序数组
 * 在传递给函数之前，nums 在预先未知的某个下标 k（0 <= k < nums.length）上进行了旋转，
 * 使数组变为 [nums[k], nums[k+1], ..., nums[n-1], nums[0], nums[1], ..., nums[k-1]]（下标从 0 开始计数）。
 * 例如，[0,1,2,4,5,6,7] 在下标 3 处经旋转后可能变为[4,5,6,7,0,1,2] 。
 * 给你旋转后的数组 nums 和一个整数 target ，如果 nums 中存在这个目标值 target ，则返回它的下标，否则返回-1
 *
 * 输入：nums = [4,5,6,7,0,1,2], target = 0
 * 输出：4
 * @author caoyang
 */
public class Leetcode33 {
    public static int search(int[] nums, int target) {
        if(nums == null || nums.length == 0){ return -1; }
        if(nums.length == 1){
            return nums[0] == target ? 0 : -1;
        }
        return findTarget(nums, target, 0, nums.length-1);
    }
    public static int findTarget(int[] nums, int target, int start, int end){

        if(target < nums[start] && target > nums[end]){
            return -1;
        }
        if(nums[start] < nums[end] && nums[end] < target){
            return -1;
        }

        if(start >= end){
            return nums[start] == target ? start : -1;
        }
        int middle = start + ((end - start) >> 1);
        int left = findTarget(nums, target, start, middle);
        int right = findTarget(nums, target, middle+1, end);
        return Math.max(left, right);
    }

    public static void main(String[] args) {
        int[] nums = {4,5,6,7,0,1,2};
        int target = 0;
        int res = search(nums, target);
        System.out.println(res);
    }
}
