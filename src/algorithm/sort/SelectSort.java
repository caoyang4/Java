package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.System.*;

/**
 * 选择排序
 * @author caoyang
 */
public class SelectSort {
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
            out.println("选择排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
    }

    public static void sort(int[] arr){
        if(arr == null || arr.length < 2){
            return;
        }
        for(int i=0; i<arr.length-1; i++){
            for(int j=i+1; j<arr.length; j++){
                if(arr[i] > arr[j]){
                    SortUtils.swap(arr, i, j);
                }
            }
        }
    }
}
