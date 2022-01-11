package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import static java.lang.System.*;

/**
 * 选择排序
 * @author caoyang
 */
public class SelectSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 5, 4, 8, 3, 2 ,5, 1};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        sort(arr);
        out.println("选择排序后：");
        SortUtils.printArr(arr);
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
