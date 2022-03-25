package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.out;

/**
 * 桶排序
 * @author caoyang
 */
public class BucketSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 5, 4, 8, 3, 2 ,5, 1, 12, 15, 19};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        sort(arr);
        out.println("选择排序后：");
        SortUtils.printArr(arr);
    }
    public static void sort(int[] arr){
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (int ele : arr) {
            max = Math.max(max, ele);
            min = Math.min(min, ele);
        }
        // 初始化桶
        int bucketSize = (max - min) / arr.length + 1;
        List<List<Integer>> bucket = new ArrayList<>();
        for (int i = 0; i < bucketSize; i++) {
            bucket.add(new ArrayList<>());
        }
        // 将元素放进桶中
        for (int ele : arr) {
            int num = (ele - min) / arr.length;
            bucket.get(num).add(ele);
        }
        // 对每个桶中元素排序
        for (int i = 0; i < bucketSize; i++) {
            Collections.sort(bucket.get(i));
        }
        // 将桶中的元素赋值到原序列
        int index = 0;
        for(int i = 0; i < bucketSize; i++){
            for(int j = 0; j < bucket.get(i).size(); j++){
                arr[index++] = bucket.get(i).get(j);
            }
        }
    }
}
