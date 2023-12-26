package src.algorithm.newcoder;

import java.util.Scanner;

/**
 * 处理牛客网输入
 * @author caoyang
 * @create 2023-12-25 17:51
 */
public class HandleNewCoderInput1 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        System.out.println(n);
        int m = scanner.nextInt();
        System.out.println(m);

        String a = scanner.next();
        System.out.println(a);
        String b = scanner.next();
        System.out.println(b);

        double d = scanner.nextDouble();
        System.out.printf("%.2f", d);

    }

}
