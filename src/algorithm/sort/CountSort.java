package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import static java.lang.System.out;

/**
 * 计数排序
 * 继续桶排序思想
 * 适用范围：数据量大范围小
 *  员工年龄排序，高考分数排序
 * @author caoyang
 */
public class CountSort {
    public static void main(String[] args) {
        int[] arr = new int[]{9, 8, 7, 7, 6, 6, 5};
        out.println("原始数组：");
        SortUtils.printArr(arr);
        sort(arr);
        // arr = sort1(arr);
        out.println("选择排序后：");
        SortUtils.printArr(arr);
    }

    public static void sort(int[] arr){
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
    public static int[] sort1(int[] arr){
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
        for (int i = 0; i < arr.length; i++) {
            result[count[arr[i]-min]-- - 1] = arr[i];
        }
        return result;
    }

}
