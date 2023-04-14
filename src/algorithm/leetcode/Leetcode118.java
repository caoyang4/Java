package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 118. 杨辉三角
 *
 * @author caoyang
 */
public class Leetcode118 {
    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> result = new ArrayList<>();
        result.add(Collections.singletonList(1));
        for (int i = 1; i < numRows; i++) {
            List<Integer> pre = result.get(i-1);
            List<Integer> tmp = new ArrayList<>();
            for (int j = 0; j <= pre.size(); j++) {
                if (j == 0 || j == pre.size()){
                    tmp.add(pre.get(0));
                } else {
                    tmp.add(pre.get(j-1)+pre.get(j));
                }
            }
            result.add(tmp);
        }
        return result;
    }
}
