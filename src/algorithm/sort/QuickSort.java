package src.algorithm.sort;
import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.Math.*;
import static java.lang.System.*;

/**
 * 快速排序
 * 三色国旗问题
 * @author caoyang
 */
public class QuickSort {
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
            out.println("快速排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
    }
    public static void sort(int[] arr){
        if(arr == null || arr.length < 2){
            return;
        }
        quickSort(arr, 0, arr.length-1);
    }
    public static void quickSort(int[] arr, int L, int R){
        if(L < R){
            int random = (L + (int) (random() * (R - L + 1)));
            SortUtils.swap(arr, random, R);
            int[] p = partition(arr, L, R);
            quickSort(arr, L, p[0] - 1);
            quickSort(arr, p[1]+1, R);
        }

    }
    public static int[] partition(int[] arr, int L, int R){
        // 将小值向左挤压，大值向右挤压，相等值自然被挤压至中间区域
        int less = L - 1;
        int more = R;
        while (L < more){
             if(arr[L] < arr[R]){
                 // 小于基准值时，左指针向右移动，并移动后的左指针和迭代器器值交换，迭代器向右移动
                 SortUtils.swap(arr, ++less, L++);
            }else if(arr[L] > arr[R]){
                 // 大于基准值时，右指针向左移动，并将移动后的右指针数和迭代器当前数交换，迭代器不动
                 SortUtils.swap(arr, --more, L);
            }else {
                 // 等于基准值时，迭代器向右移动移动即可
                 L++;
            }
        }
        SortUtils.swap(arr, more, R);
        return new int[]{less+1, more};
    }

    public static void quickSort1(int[] a, int low, int high) {
        //已经排完
        if (low >= high) {
            return;
        }
        int left = low;
        int right = high;

        //保存基准值
        int pivot = a[left];
        while (left < right) {
            //从后向前找到比基准小的元素
            while (left < right && a[right] >= pivot) {
                right--;
            }
            a[left] = a[right];
            //从前往后找到比基准大的元素
            while (left < right && a[left] <= pivot) {
                left++;
            }
            a[right] = a[left];
        }
        // 放置基准值，准备分治递归快排
        a[left] = pivot;
        quickSort1(a, low, left - 1);
        quickSort1(a, left + 1, high);
    }
}
