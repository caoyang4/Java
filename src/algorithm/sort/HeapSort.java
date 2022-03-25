package src.algorithm.sort;

import src.algorithm.utils.SortUtils;

import java.util.Random;

import static java.lang.System.out;

/**
 * 堆排序
 * 基于完全二叉树
 * @author caoyang
 */
public class HeapSort {
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
            out.println("堆排序后：");
            SortUtils.printArr(arr);
            out.println();
        }
    }
    public static void sort(int[] arr){
        /*
        * 第一步：对原数组构建大顶堆
        * 从最后一个非叶子结点开始调整堆
        * */
        for (int i = (arr.length >> 1); i >= 0; i--) {
            heapfy(arr, i, arr.length);
        }

        for (int i = arr.length-1; i >= 0; i--) {
            // 交换堆顶元素与末尾元素
            SortUtils.swap(arr, 0, i);
            // 重新调整堆结构
            heapfy(arr, 0, i);
        }
    }

    /**
     * 堆化
     */
    public static void heapfy(int[] arr, int idx, int length){
        for (int k = 2*idx + 1; k < length; k = 2*k + 1) {
            // 左节点小于右节点时
            if (k+1 < length && arr[k] < arr[k+1]){
                k++;
            }
            if (arr[idx] < arr[k]){
                SortUtils.swap(arr, idx, k);
                // 继续向下筛选
                idx = k;
            } else {
                // 如果父结点的值已经大于子结点的值，则直接结束
                break;
            }

        }
    }
}
