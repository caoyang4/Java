package src.jvm;

import java.util.HashSet;
import java.util.Set;

/**
 * jdk8以后，常量池被移至堆中，一般不会发生OOM
 * -XX:-UseGCOverheadLimit -XX:MetaspaceSize=6m -XX:MaxMetaspaceSize=6m -Xms6m -Xmx6m
 */
public class StringConstantPoolOOMTest {
    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        long times = 0;
        for (;;){
            set.add(String.valueOf(times++).intern());
        }
    }
}
