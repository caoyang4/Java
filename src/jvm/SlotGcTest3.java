package src.jvm;

/**
 * -XX:+PrintGCDetails 打印 gc 信息
 * @author caoyang
 */
public class SlotGcTest3 {
    public static void main(String[] args) {
        {
            byte[] holder = new byte[64 * 1024 * 1024];
        }
        int i = 0;
        // [GC (System.gc())  70786K->66424K(251392K), 0.0030879 secs]
        // [Full GC (System.gc())  66424K->662K(251392K), 0.0070623 secs]
        // gc 回收成功
        System.gc();
    }
}
