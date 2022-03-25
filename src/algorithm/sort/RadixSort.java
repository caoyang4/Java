package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.Arrays;
import java.util.Random;

import static java.lang.System.out;

/**
 * 基数排序
 * 非比较排序，基于桶排序思想，多关键字排序
 * @author caoyang
 */
public class RadixSort {
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
            out.println("基数排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
    }

    /**
     * 基数排序中计数排序，需要保证数位中相等数数字位置不变动，才能迭代得到正确排序！！！
     * @param arr
     */
    public static void sort(int[] arr){
        int max = Integer.MIN_VALUE;
        for (int val : arr) {
            max = Math.max(max, val);
        }
        int pow = 0;
        while (max > 0){
            max /= 10;
            pow++;
        }
        // 数字只有 0-9，故数组长度为10即可
        int[] count = new int[10];
        int[] result = new int[arr.length];
        for (int i = 0; i < pow; i++) {
            int division = (int) Math.pow(10, i);
            for (int ele : arr) {
                int num = ele / division % 10;
                count[num]++;
            }
            for (int j = 1; j < count.length; j++) {
                count[j] += count[j-1];
            }
            for (int j = arr.length-1; j >= 0; j--) {
                int num = arr[j] / division % 10;
                result[--count[num]] = arr[j];
            }

            System.arraycopy(result, 0, arr, 0, arr.length);
            // 将 count 重置为 0 数组
            Arrays.fill(count, 0);
        }
    }
}
