package src.algorithm.newcoder;

import com.google.common.collect.Lists;

import java.util.Scanner;

/**
 * 处理牛客网输入
 * @author caoyang
 * @create 2023-12-25 17:51
 */
public class HandleNewCoderInput2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String e = scanner.next();
            System.out.println(e);
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] words = line.split(",");
            System.out.println(Lists.newArrayList(words));
        }

    }

}
