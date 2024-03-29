package src.algorithm.sort;
import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.System.*;

/**
 * @author caoyang
 */
public class MergeSort {
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
            out.println("归并排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
    }
    public static void sort(int[] arr){
        if(arr == null || arr.length < 2){
            return;
        }
        mergeSort(arr, 0, arr.length-1);
    }

    public static void mergeSort(int[] arr, int start, int end){
        if(start >= end){
            return;
        }
        int mid = start + ((end-start) >> 1);
        // 分段排序
        mergeSort(arr, start, mid);
        mergeSort(arr, mid+1, end);
        // 归并
        merge(arr, start, mid, end);
    }
     public static void merge(int[] arr, int low, int mid, int high){
        int[] temp = new int[high-low+1];
        int i = low;
        int j = mid + 1;
        int k = 0;
        // 比较两个子序列，将较小数放进临时数组中
        while(i <= mid && j <= high){
           temp[k++] = arr[i] <= arr[j] ? arr[i++] : arr[j++];
        }

        while (i <= mid){
            temp[k++] = arr[i++];
        }
        while (j <= high){
            temp[k++] = arr[j++];
        }

        for(int t=0; t<temp.length; t++){
            arr[low+t] = temp[t];
        }

     }
}
