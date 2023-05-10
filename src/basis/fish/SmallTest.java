package src.basis.fish;

/**
 * @author caoyang
 * @create 2023-05-05 21:44
 */
public class SmallTest {

    public static void test(int[] arr){
        arr[0] = 6;
    }
    public static void main(String[] args) {
        int[] arr = {1,2,3};
        System.out.println(arr[0]);
        test(arr);
        System.out.println(arr[0]);
        int i = 6;
        int j = 2;
        double x = (double) i / j;
        System.out.println(x);
    }
}
