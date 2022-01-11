package src.juc.pratice;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * 线程顺序打印 A1B2C3...
 * @author caoyang
 */
public class PrintA1B2 {
    static char[] alphabets = new char[] {
            'A', 'B', 'C',
            'D', 'E', 'F',
            'G', 'H', 'I',
            'J', 'K', 'L',
            'M', 'N', 'O',
            'P', 'Q', 'R',
            'S', 'T', 'U',
            'V', 'W', 'X',
            'Y', 'Z'
    };
    static Thread t1 = null;
    static Thread t2 = null;
    public static void main(String[] args) {

        t1 = new Thread(() -> {
//            System.out.println("t1 启动");
            for (char alphabet : alphabets) {
                System.out.print(alphabet);
                LockSupport.unpark(t2);
                LockSupport.park();
            }
        });

        t2 = new Thread(() -> {
//            System.out.println("t2 启动");
            for (int i = 0; i < alphabets.length; i++) {
                LockSupport.park();
                System.out.print(" "+(i + 1));
                System.out.println();
                LockSupport.unpark(t1);
            }
        });

        t2.start();
        t1.start();
    }
}
