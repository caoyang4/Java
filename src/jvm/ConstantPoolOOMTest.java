package src.jvm;

import java.util.HashSet;
import java.util.Set;

/**
 * jdk8以后，常量池被移至堆中，一般不会发生OOM
 */
public class ConstantPoolOOMTest {
    public static void main(String[] args) {
        String t = "java";
        String t1 = new StringBuilder("ja").append("va").toString();
        // 不符合首次遇到原则，结果为 false
        System.out.println(t1.intern() == t1);

        String s1 = new StringBuilder("he").append("llo").toString();
        // 符合首次遇到原则，结果为 true
        System.out.println(s1.intern() == s1);

        Set<String> set = new HashSet<>();
        long times = 0;
        for (;;){
            // 一般不会发生OOM
            set.add(String.valueOf(times++).intern());
        }
    }
}
