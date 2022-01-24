package src.basis.fish;

/**
 * @author caoyang
 */
public class InterestTest {

    public static void general(){
        int a=10;
        // m=7+a   a=a+1
        int m=7+a++;
        //11
        System.out.println(a);
        //17
        System.out.println(m);
    }

    public static void interest(){
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
        System.out.println(i);
    }
    public static void main(String[] args) {
        general();

        interest();

        int a = 11 % 4;
        System.out.println(a);

        // (n-1) & hash
        int b = 11 & (4 - 1);
        System.out.println(b);

    }
}
