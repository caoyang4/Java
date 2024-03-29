package src.basis.fish;

import java.util.Arrays;
import java.util.Random;

/**
 * @author caoyang
 */
public class TestFinal {

    // 编译期常量
    final int i = 1;
    static final int J = 1;
    static final int[] ARR = {1,2};
    final int[] a = {1,2,3,4};
    // 非编译期常量
    static Random r = new Random();

    // k的值由随机数对象决定，所以不是所有的final修饰的字段都是编译期常量，只是k的值在被初始化后无法被更改
    final int k1 = r.nextInt(10);
    static final int k2 = r.nextInt(10);
    public static void main(String[] args) {
        // k1 变化， k2 变化
        // static final所修饰的字段仅占据内存的一个一份空间，一旦被初始化之后便不会被更改
        TestFinal t1 = new TestFinal();
        System.out.println("k1="+t1.k1+" k2="+t1.k2);
        TestFinal t2 = new TestFinal();
        System.out.println("k1="+t2.k1+" k2="+t2.k2);
        // 引用不可变，引用指向的对象可能会变
        final A a = new A();
        System.out.println(a.i);
        a.i = 2;
        System.out.println(a.i);

        System.out.println(Arrays.toString(ARR));
        ARR[0] = 6;
        System.out.println(Arrays.toString(ARR));
    }
    static class A {
        public int i;
    }
}


