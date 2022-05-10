package src.jvm;

/**
 * 常量池 GC
 * -Xms16m -Xmx16m -XX:+PrintStringTableStatistics -XX:+PrintGCDetails
 */
public class StringGCTest {
    public static void main(String[] args) {
        final int times = 100000;
        for (int i = 0; i < times; i++) {
            String.valueOf(i).intern();
        }
        // Number of buckets       :     20011 =    160088 bytes, avg   8.000
        // Number of entries       :     12786 =    306864 bytes, avg  24.000
        // Number of literals      :     12786 =    492760 bytes, avg  38.539
        // 底层是哈希表存储字符串常量
    }
}
