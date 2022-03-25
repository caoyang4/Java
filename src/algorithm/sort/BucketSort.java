package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import static java.lang.System.out;

/**
 * 桶排序
 * @author caoyang
 */
public class BucketSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 5, 4, 8, 3, 2 ,5, 1};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        sort(arr);
        out.println("选择排序后：");
        SortUtils.printArr(arr);
    }
    public static void sort(int[] arr){

    }
}
