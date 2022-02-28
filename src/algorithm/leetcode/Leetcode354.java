package src.algorithm.leetcode;

import java.util.Arrays;

/**
 * 354. 俄罗斯套娃信封问题
 * 给你一个二维整数数组 envelopes ，其中 envelopes[i] = [wi, hi] ，表示第 i 个信封的宽度和高度。
 * 当另一个信封的宽度和高度都比这个信封大的时候，这个信封就可以放进另一个信封里，如同俄罗斯套娃一样。
 * 请计算 最多能有多少个 信封能组成一组“俄罗斯套娃”信封（即可以把一个信封放到另一个信封里面）。
 * 注意：不允许旋转信封
 *
 * 输入：envelopes = [[5,4],[6,4],[6,7],[2,3]]
 * 输出：3
 * 解释：最多信封的个数为 3, 组合为: [2,3] => [5,4] => [6,7]
 *
 * @author caoyang
 */
public class Leetcode354 {
    /**
     * 二维数组可以先把一维数组排序，再处理
     * 先将信封按照宽度排序，转换求解高度最长上升子序列
     * @param envelopes
     * @return
     */
    public static int maxEnvelopes(int[][] envelopes) {
        if(envelopes == null || envelopes.length < 1){
            return 0;
        }
        Arrays.sort(envelopes, (o1, o2) -> o1[0] == o2[0] ? o1[1] - o2[1] : o1[0] - o2[0]);

        int[] f = new int[envelopes.length];
        int result = 0;
        for (int i = 0; i < envelopes.length; i++) {
            f[i] = 1;
            for (int j = 0; j < i; j++) {
                if (envelopes[j][0] < envelopes[i][0] && envelopes[j][1] < envelopes[i][1]){
                    f[i] = Math.max( f[j] + 1, f[i]);
                }
            }
            result = Math.max(result, f[i]);
        }
        return result;
    }

    public static void main(String[] args) {
        int[][] envelopes = {{10,17},{10,19},{16,2},{19,18},{5,6}};
        int res = maxEnvelopes(envelopes);
        System.out.println(res);
    }
}
