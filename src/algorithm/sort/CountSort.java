package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.System.out;

/**
 * 计数排序
 * 非比较排序，基于桶排序思想
 * 适用范围：数据量大范围小
 *  员工年龄排序，高考分数排序
 * @author caoyang
 */
public class CountSort {
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
            // sort1(arr);
            arr = sort(arr);
            out.println("计数排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
    }

    public static void sort1(int[] arr){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int value : arr) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        // 取值范围[min, max]
        int[] count = new int[max - min + 1];
        for (int value : arr) {
            // 将值作为索引进行计数
            count[value-min]++;
        }
        int k = 0;
        for (int i = 0; i < count.length; i++) {
            for (int j = 0; j < count[i]; j++) {
                arr[k++] = min+i;
            }
        }
    }
    public static int[] sort(int[] arr){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int value : arr) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        // 取值范围[min, max]
        int[] count = new int[max - min + 1];
        for (int value : arr) {
            // 将值作为索引进行计数
            count[value-min]++;
        }
        for (int i=1; i<count.length; i++) {
            count[i] += count[i-1];
        }
        // 创建结果数组
        int[] result = new int[arr.length];
        // 原始数组中的相同元素按照本来的顺序的排列，从后往前遍历
        for (int i=arr.length-1; i >= 0; i--) {
            result[--count[arr[i] - min]] = arr[i];
        }
        return result;
    }

}
