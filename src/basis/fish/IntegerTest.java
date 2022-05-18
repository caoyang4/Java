package src.basis.fish;

import org.junit.Test;

/**
 * Integer 测试
 * @author caoyang
 */
public class IntegerTest {

    @Test
    public void integerTest1(){
        Integer a = 127;
        Integer b = 127;
        Integer c = new Integer(127);
        int d =127;
        // true
        System.out.println("127 a==b: " + (a==b));
        // false
        System.out.println("127 b==c: " + (b==c));
        // true
        System.out.println("127 b==d: " + (b==d));
        // true
        System.out.println("127 c==d: " + (c==d));
        // true
        System.out.println("127 a==d: " + (a==d));

        Integer e = 128;
        Integer f = 128;
        int g = 128;
        // false
        System.out.println("128 e==f: " + (e==f));
        // true
        System.out.println("128 e==g: " + (e==g));
    }

    @Test
    public void integerTest2(){
        Integer a = 1;
        Integer b = 2;
        Integer c = 3;
        Integer d = 3;
        Integer e = 321;
        Integer f = 321;
        Long g = 3L;
        // true
        System.out.println("c==d: " + (c==d));
        // false
        System.out.println("e==f: " + (e==f));
        // true
        System.out.println(c == (a+b));
        // true 类型和数值一样
        System.out.println(c.equals(a+b));
        // true 比较数值
        System.out.println(g == (a+b));
        // false 类型不一样
        System.out.println(g.equals(a+b));
    }

    @Test
    public void integerTest3(){
        int a=10;
        // m=7+a   a=a+1
        int m=7+a++;
        //11
        System.out.println(a);
        //17
        System.out.println(m);
    }

    @Test
    public void integerTest4(){
        /**
         *     iconst_0
         *     istore 0     0赋值给i
         *     iload 0      压栈
         *     iinc 0,1     i自增
         *     istore 0     弹栈 0
         *     getstatic 'java/lang/System.out','Ljava/io/PrintStream;'
         *     iload 0      0 赋值给i，自增白忙活
         */
        int i = 0;
        // i++ 更改的值不会被使用
        i = i++;
        System.out.println("interest: "+i);
    }

    @Test
    public void divZero(){
        // Infinity
        System.out.println((1.0)/(0.0));
    }

}
