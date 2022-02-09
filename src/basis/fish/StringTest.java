package src.basis.fish;

/**
 * @author caoyang
 */
public class StringTest {
    public static void main(String[] args) {
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


        String x = "java";
        String y = new StringBuilder().append("ja").append("va").toString();
        // 由于"java"在常量池已出现过，y.intern 返回 x 的地址，与 y 不同
        System.out.println(y.intern() == x);
        System.out.println(y.intern() == y);

        String z = new StringBuilder().append("wor").append("ld").toString();
        System.out.println(z.intern() == z);

        /*
        字符串长度是由UTF-16编码单元个数决定
        char s = '𝄞'; 编译检查会提示字符过多，𝄞 实际占两个字符
         */
        String B = "𝄞";
        // 音符字符的UTF-16编码
        String C = "\uD834\uDD1E";
        System.out.println(C);
        System.out.println(B.length());


    }
}
