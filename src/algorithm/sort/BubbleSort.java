package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import static java.lang.System.*;

/**
 * 冒泡排序
 * @author caoyang
 */
public class BubbleSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 5, 4, 8, 3, 2 ,5, 1};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        sort(arr);
        out.println("冒泡排序后：");
        SortUtils.printArr(arr);


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
