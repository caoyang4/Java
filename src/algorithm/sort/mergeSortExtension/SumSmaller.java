package sort.mergeSortExtension;

import utils.SortUtils;

import static java.lang.System.*;

/**
 * 数组左侧小数和
 * @author caoyang
 */
public class SumSmaller {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 5, 4, 8, 3, 2 ,5, 1};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        int sum = mergeSort(arr, 0, arr.length-1);
        out.println("排序求和后：");
        SortUtils.printArr(arr);
        out.println(sum);
    }
    public static int mergeSort(int[] arr, int start, int end){
        int mid = start + ((end - start) >> 1);
        if( start >= end){
            return 0;
        }
        return mergeSort(arr, start, mid) +
                mergeSort(arr, mid+1, end) +
                merge(arr, start, mid, end);
    }
    public static int merge(int[] arr, int start, int mid, int end){
        int left = start;
        int right = mid + 1;
        int[] temp = new int[end - start + 1];
        int i = 0;
        int sum = 0;
        while (left <= mid && right <= end){
            if(arr[left] < arr[right]){
                sum += arr[left] * (end - right + 1);
                temp[i++] = arr[left++];
            }else {
                temp[i++] = arr[right++];
            }
        }
        while (left <= mid){
            temp[i++] = arr[left++];
        }
        while (right <= end){
            temp[i++] = arr[right++];
        }
        for(i=0; i<temp.length; i++){
            arr[start+i] = temp[i];
        }
        return sum;
    }

}
