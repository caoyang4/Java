package src.algorithm.utils;

/**
 * 通用工具类
 * @author caoyang
 */
public class CommonUtils {
    public static void printArray(int[] arr){
        System.out.print("[ ");
        for (int i : arr) {
            System.out.print(i + " ");
        }
        System.out.println("]");
    }
}
