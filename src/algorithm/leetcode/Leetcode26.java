package src.algorithm.leetcode;

/**
 * 26. 删除有序数组中的重复项
 *
 * 给你一个升序排列的数组 nums ，请你原地删除重复出现的元素，使每个元素只出现一次 ，
 * 返回删除后数组的新长度。元素的相对顺序应该保持一致
 *
 * @author caoyang
 */
public class Leetcode26 {
    public static int removeDuplicates(int[] nums) {
        int len = nums.length;
        if(len == 1){
            return len;
        }
        for (int j = 0; j < len-1; j++) {
            if(nums[j] == nums[len-1]){
                return j+1;
            }
            int k = j;
            while (nums[j] == nums[k+1]){
                k++;
            }
            if(j != k){
                for(int i = k+1; i < len; i++) {
                    nums[i-k+j] = nums[i];
                }
            }
        }
        return len;
    }

    public static void main(String[] args) {
        int[] nums = {0,0,1,1,1,2,2,3,3,4,5,5};
        int res = removeDuplicates(nums);
        System.out.println(res);
        for (int num : nums) {
            System.out.print(num + "\t");
        }
    }
}
