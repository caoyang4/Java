package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import static java.lang.System.out;

/**
 * 希尔排序
 * 插入排序改进版
 * @author caoyang
 */
public class ShellSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 5, 4, 8, 3, 2 ,5, 1};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        sort(arr);
        out.println("选择排序后：");
        SortUtils.printArr(arr);
    }
    public static void sort(int[] arr){
        // 希尔间隔
        // 二分序列
        // int h = arr.length >> 1;
        // knuth 序列
        int h = 1;
        while (h <= arr.length / 3){
            h = h * 3 + 1;
        }
        for (int gap = h; gap > 0; gap = (gap-1) / 3) {
            for (int i = gap; i < arr.length; i++) {
                for (int j = i; j >= gap && arr[j] < arr[j-gap]; j-=gap) {
                    SortUtils.swap(arr, j, j-gap);
                }
            }
        }
    }
}
