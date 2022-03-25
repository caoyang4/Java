package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.System.out;

/**
 * 希尔排序
 * 插入排序改进版
 * @author caoyang
 */
public class ShellSort {
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
            out.println("希尔排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
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
