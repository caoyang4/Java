package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 315. 计算右侧小于当前元素的个数
 * 输入：nums = [5,2,6,1]
 * 输出：[2,1,1,0]
 * @author caoyang
 */
public class Leetcode315 {
    public static List<Integer> countSmaller(int[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        // 索引数组
        // 不管元素排序后被放在哪个位置，其在原数组的位置是不变的，才能正确对应count[i]
        int[] indexes = new int[n];
        for(int i = 0; i < n; i++){
            indexes[i] = i;
        }
        count(result, indexes, nums, 0, nums.length-1);
        List<Integer> ans = new ArrayList<>();
        for (int i : result) {ans.add(i);}
        return ans;
    }
    public static void count(int[] result, int[] indexes, int[] nums, int start, int end){
        if(start >= end){
            return;
        }
        int middle = start + ((end - start) >> 1);
        count(result, indexes, nums, start, middle);
        count(result, indexes, nums, middle+1, end);
        merge(result, indexes, nums, start, middle, end);
    }
    public static void merge(int[] result, int[] indexes, int[] nums, int start, int middle, int end){
        int left = start;
        int right = middle+1;
        int[] tmp = new int[end-start+1];
        int[] tmpIndex = new int[end-start+1];
        int index = 0;
        while (left <= middle && right <= end){
            if(nums[left] > nums[right]){
                //右半部分小于nums[left]元素的数目
                result[indexes[left]] += end-right+1;
                //记录元素位置的改变
                tmpIndex[index] = indexes[left];
                tmp[index++] = nums[left++];
            } else {
                tmpIndex[index] = indexes[right];
                tmp[index++] = nums[right++];
            }
        }

        while (left <= middle){
            tmpIndex[index] = indexes[left];
            tmp[index++] = nums[left++];
        }
        while (right <= end){
            tmpIndex[index] = indexes[right];
            tmp[index++] = nums[right++];
        }
        for (int i = 0; i < tmp.length; i++) {
            nums[start+i] = tmp[i];
            indexes[start+i] = tmpIndex[i];
        }
    }

    public static void main(String[] args) {
        int[] nums = {0,2,1};
        List<Integer> result = countSmaller(nums);
        System.out.println(result);
    }
}
