package src.basis.fish;

import org.junit.Test;

/**
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
        System.out.println(y.intern() == x);
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
}
