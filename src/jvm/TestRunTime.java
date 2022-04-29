package src.jvm;

/**
 * -Xms60m -Xmx60m -XX:+PrintGCDetails
 * @author caoyang
 */
public class TestRunTime {
    public static void main(String[] args) {
        Runtime rt = Runtime.getRuntime();
        long xms = rt.totalMemory() / 1024 / 1024;
        long xmx = rt.maxMemory() / 1024 / 1024;
        // -Xms60m，结果为 57m
        // 原因是s0(2560K) 和 s1(2560K)，由于标记复制算法，其中有一个是要空着的
        // Heap
        // PSYoungGen      total 17920K, used 3743K [0x00000007bec00000, 0x00000007c0000000, 0x00000007c0000000)
        //  eden space 15360K, 24% used [0x00000007bec00000,0x00000007befa7e28,0x00000007bfb00000)
        //  from space 2560K, 0% used [0x00000007bfd80000,0x00000007bfd80000,0x00000007c0000000)
        //  to   space 2560K, 0% used [0x00000007bfb00000,0x00000007bfb00000,0x00000007bfd80000)
        // ParOldGen       total 40960K, used 0K [0x00000007bc400000, 0x00000007bec00000, 0x00000007bec00000)
        //  object space 40960K, 0% used [0x00000007bc400000,0x00000007bc400000,0x00000007bec00000)
        // Metaspace       used 3158K, capacity 4496K, committed 4864K, reserved 1056768K
        //  class space    used 338K, capacity 388K, committed 512K, reserved 1048576K
        System.out.println("-Xms"+xms+"M");
        System.out.println("-Xmx"+xmx+"M");


    }
}
