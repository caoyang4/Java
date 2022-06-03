package src.basis.fish;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * new String("a")创建几个对象？
 * 2个： new String() 和 常量"a"
 * new String("a") + new String("b") +创建几个对象？
 * 5个：2*2+1 另创建了 StringBuilder对象 做拼接
 * @author caoyang
 */
public class StringTest1 {
    @Test
    public void stringTest1() {
        String a = new String("xyz");
        String b = new String("xyz");
        // false
        System.out.println(a == b);

        String c1 = "xyz";
        String c2 = "xyz";
        String d = new String("xyz").intern();

        // false
        System.out.println(a == c1);
        // true
        System.out.println(c1 == c2);
        // true
        System.out.println(c1 == d);

    }

    @Test
    public void stringTest2(){
        String x = "java";
        String y = new StringBuilder().append("ja").append("va").toString();
        // 由于"java"在常量池已出现过，y.intern 返回 x 的地址，与 y 不同
        // true
        System.out.println(y.intern() == x);
        // false
        System.out.println(y.intern() == y);

        String z = new StringBuilder().append("wor").append("ld").toString();
        // true
        System.out.println(z.intern() == z);
    }

    @Test
    public void stringTest3() {
        final String a = "java";
        final String b = "world";
        String c = "javaworld";
        // a、b 都是常量引用，内部会编译器优化
        String d = a + b;
        // true
        System.out.println(c == d);
    }

    @Test
    public void stringTest4(){
        /*
        字符串长度是由UTF-16编码单元个数决定
        char s = '𝄞'; 编译检查会提示字符过多，𝄞 实际占两个字符
         */
        String a = "𝄞";
        // 音符字符的UTF-16编码
        String b = "\uD834\uDD1E";
        System.out.println(b);
        System.out.println(a.length());
    }

    @Test
    public void stringTest5(){
        long start = System.currentTimeMillis();
        String str = "";
        for (int i = 0; i < 10000; i++) {
            // 内部会不断新创建StringBuilder做拼接，然后toString方法返回，toString 内部会 new String()
            str = str + i;
        }
        // cost 359ms
        System.out.println("cost "+(System.currentTimeMillis()-start));
    }

    @Test
    public void stringTest6(){
        long start = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            builder.append(i);
        }
        // cost 2ms
        System.out.println("cost "+(System.currentTimeMillis()-start));
    }

    private void printStrings(String... strs){
        for (String str : strs) {
            System.out.print(str + " ");
        }
        System.out.println();
    }
    @Test
    public void stringTest7(){
        printStrings("a","b","c");
        printStrings(new String[]{"1", "2", "3"});
    }

    public static void main(String[] args) {
        String s1 = new String("1");
        s1.intern();
        String s2 = "1";
        // false
        System.out.println(s1 == s2);
        // 由于"1"已存在于常量池，等价于 s3 = new String("11")
        String s3 = new String("1") + new String("1");
        // 常量池不存在"11"，调用intern()复制一份new String("11")的引用地址，放入字符串常量池
        s3.intern();
        String s4 = "11"; // 返回new String("11")在字符串常量池的地址
        // true
        System.out.println(s3 == s4);
    }
}
