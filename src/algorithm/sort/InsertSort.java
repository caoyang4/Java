package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.System.out;

/**
 * 插入排序
 * @author caoyang
 */
public class InsertSort {
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
            out.println("插入排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
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
