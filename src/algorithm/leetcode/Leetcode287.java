package src.algorithm.leetcode;

/**
 * 287. 寻找重复数
 * 给定一个包含 n + 1 个整数的数组 nums ，其数字都在 [1, n] 范围内（包括 1 和 n），可知只有一个重复的整数，该整数至少存在一个
 * 输入：nums = [3,1,3,4,2]
 * 输出：3
 * @author caoyang
 */
public class Leetcode287 {
    public static int findDuplicate1(int[] nums) {
        for (int i = 0; i < nums.length-1; i++) {
            for (int j = i+1; j < nums.length; j++) {
                if (nums[i] == nums[j]){
                    return nums[i];
                }
            }
        }
        return 0;
    }
    public static int findDuplicate(int[] nums) {
        int slow = 0;
        int fast = 0;
        while (true){
            fast = nums[nums[fast]];
            slow = nums[slow];
            if (fast ==  slow){
                fast = 0;
                while (nums[fast] != nums[slow]){
                    fast = nums[fast];
                    slow = nums[slow];
                }
                return nums[slow];
            }
        }
    }

    public static void main(String[] args) {
        int[] nums = {3,1,3,4,2};
        int reuslt = findDuplicate(nums);
        System.out.println(reuslt);
    }
}
