package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.System.*;

/**
 * 冒泡排序
 * @author caoyang
 */
public class BubbleSort {
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
            out.println("冒泡排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
    }

    public static void sort(int[] arr){
        if(arr == null || arr.length < 2){
            return;
        }
        for(int i=arr.length-1; i>0; i--){
            // 每次将数组中最大数向右推进
            for(int j=0; j<i; j++){
                if(arr[j] > arr[j+1]){
                    SortUtils.swap(arr, j, j+1);
                }
            }
        }
    }

}
