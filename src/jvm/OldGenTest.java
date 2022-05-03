package src.jvm;

/**
 * -Xms60m -Xmx60m -XX:NewRatio=2 -XX:SurvivorRatio=8 -XX:+PrintGCDetails
 * old:40m eden:16m s0:2m s1:2m
 * 大对象直接放入老年代
 */
public class OldGenTest {
    public static void main(String[] args) {
        // 20m
        //  ParOldGen   total 40960K, used 20480K [0x00000007bc400000, 0x00000007bec00000, 0x00000007bec00000)
        byte[] bytes = new byte[1024 * 1024 * 20];
    }
}
