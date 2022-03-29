package src.algorithm.leetcode;


import java.util.Arrays;
import java.util.Comparator;

/**
 * 56. 合并区间
 * 以数组 intervals 表示若干个区间的集合，其中单个区间为 intervals[i] = [start, end] 。
 * 请你合并所有重叠的区间，并返回一个不重叠的区间数组，该数组需恰好覆盖输入中的所有区间
 *
 * 输入：intervals = [[1,3],[2,6],[8,10],[15,18]]
 * 输出：[[1,6],[8,10],[15,18]]
 *
 * @author caoyang
 */
public class Leetcode56 {
    public static int[][] merge(int[][] intervals) {
        int len = intervals.length;
        int[][] result1 = new int[len][2];
        Arrays.sort(intervals, (o1, o2) -> o1[0] != o2[0] ? o1[0] - o2[0] : o1[1] - o2[1]);
        int index = 0;
        result1[index] = intervals[index];
        for (int i = 1; i < len; i++) {
            if(result1[index][1] >= intervals[i][1]){
                continue;
            }
            if (result1[index][1] >= intervals[i][0]){
                result1[index][1] = intervals[i][1];
            } else {
                result1[++index] = intervals[i];
            }
        }
        int[][] result = new int[index+1][2];
        System.arraycopy(result1, 0, result, 0, index+1);
        return result;
    }

    public static void main(String[] args) {
       int[][] intervals = {{1,3},{2,6},{8,10},{15,18}};
       int[][] result = merge(intervals);
        for (int[] ints : result) {
            System.out.println(Arrays.toString(ints));
        }
    }

}
