package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.System.out;

/**
 * 桶排序
 * @author caoyang
 */
public class BucketSort {
    public static void main(String[] args) {
        final int times = 10;
        for (int i = 0; i < times; i++) {
            out.println("第"+(i+1)+"次测试：");
            final int len = 50;
            int[] arr = new int[len];
            for (int j = 0; j < len; j++) {
                arr[j] = new Random().nextInt(100);
            }
            out.println("原始数组：");
            SortUtils.printArr(arr);
            sort(arr);
            out.println("桶排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
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
