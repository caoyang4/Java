package utils;

import static java.lang.System.*;

/**
 * @author caoyang
 */
public class SortUtils {

    private SortUtils() {}
    /**
     * 数组下标交换顺序
     * @param arr 数组
     * @param i 下标i
     * @param j 下标j
     */
    public static void swap(int[] arr, int i, int j){
        if(i == j){
            return;
        }
        arr[i] = arr[i] ^ arr[j];
        arr[j] = arr[j] ^ arr[i];
        arr[i] = arr[i] ^ arr[j];
    }

    public static void printArr(int[] arr){
        for (int i : arr) {
            out.print(i+" ");
        }
        out.println();
    }
}
