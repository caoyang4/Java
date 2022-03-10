package src.algorithm.leetcode;

/**
 * 34. 在排序数组中查找元素的第一个和最后一个位置
 *
 * 给定一个按照升序排列的整数数组 nums，和一个目标值 target。找出给定目标值在数组中的开始位置和结束位置。
 * 如果数组中不存在目标值 target，返回[-1, -1]
 *
 * 输入：nums = [5,7,7,8,8,10], target = 6
 * 输出：[-1,-1]
 *
 * @author caoyang
 */
public class Leetcode34 {
    public static int[] searchRange(int[] nums, int target) {
        int[] res = {-1, -1};
        if(nums == null || nums.length == 0){
            return res;
        }
        return findTarget(nums, target,0, nums.length-1);
    }
    public static int[] findTarget(int[] nums, int target, int start, int end){
        int[] res = {-1, -1};
        if (target < nums[start] || target > nums[end]){
            return res;
        }
        if(start >= end){
            return nums[start] == target ? new int[] {start, start} : res;
        } else if(start + 1 == end){
            res[0] = nums[start] == target ? start : -1;
            res[1] = nums[end] == target ? end : res[0];
            res[0] = res[0] != -1 ? res[0] : res[1];
            return res;
        }
        int middle = start + ((end - start) >> 1);
        int[] leftRange = findTarget(nums, target, start, middle);
        int[] rightRange = findTarget(nums, target, middle+1, end);
        res = leftRange;
        if(rightRange[0] != -1){
            if(res[0] == -1){
                res[0] = rightRange[0];
            }
            res[1] = rightRange[1];
        }
        return res;
    }

    public static void main(String[] args) {
        int[] nums = {5,7,7,8,8,10};
        int target = 8;
        int[] res = searchRange(nums, target);
        for (int re : res) {
            System.out.print(re + "\t");
        }
    }
}
