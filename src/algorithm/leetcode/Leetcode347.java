package src.algorithm.leetcode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 347. 前K个高频元素
 * 给你一个整数数组 nums 和一个整数 k ，请你返回其中出现频率前 k 高的元素。你可以按任意顺序返回答案
 *
 * 输入: nums = [1,1,1,2,2,3], k = 2
 * 输出: [1,2]
 * @author caoyang
 */
public class Leetcode347 {
    public static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int num : nums) {
            map.put(num, map.getOrDefault(num,0)+1);
        }
        // 优先级队列
        PriorityQueue<Integer> queue = new PriorityQueue<>((o1, o2) -> {
            return map.get(o1) - map.get(o2);
        });
        for (int num : map.keySet()) {
            if (queue.size() < k){
                queue.add(num);
            } else {
                if(map.get(num) > map.get(queue.peek())){
                    queue.remove();
                    queue.add(num);
                }
            }
        }
        int[] res = new int[k];
        int start = 0;
        while (!queue.isEmpty() && start < k){
            res[start++] = queue.remove();
        }
        return res;
    }

    public static void main(String[] args) {
        int[] nums = {1,1,1,2,2,3};
        int k = 2;
        int[] result = topKFrequent(nums, k);
        System.out.println(Arrays.toString(result));
    }
}
