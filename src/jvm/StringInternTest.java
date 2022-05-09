package src.jvm;

public class StringInternTest {
    public static void main(String[] args) {
        String t = "java";
        String t1 = new StringBuilder("ja").append("va").toString();
        // 不符合首次遇到原则，结果为 false
        System.out.println("不符合首次遇到原则: "+(t1.intern() == t1));

        String s1 = new StringBuilder("he").append("llo").toString();
        // 符合首次遇到原则，结果为 true
        System.out.println("符合首次遇到原则: "+(s1.intern() == s1));
    }
}
