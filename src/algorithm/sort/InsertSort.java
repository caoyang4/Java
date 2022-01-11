package sort;

import utils.SortUtils;

import static java.lang.System.out;

/**
 * 插入排序
 * @author caoyang
 */
public class InsertSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 5, 4, 8, 3, 2 ,5, 1};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        sort(arr);
        out.println("插入排序后：");
        SortUtils.printArr(arr);
    }
    public static void sort(int[] arr){
        if(arr == null || arr.length < 2){
            return;
        }
        for(int i=0; i<arr.length-1; i++){
            // 通过将较小的元素向左移动而不总是交换两个元素
            for(int j=i+1; j>0 && arr[j]<arr[j-1]; j--){
                SortUtils.swap(arr, j, j-1);
            }
        }
    }
}
