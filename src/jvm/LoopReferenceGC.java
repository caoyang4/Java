package src.jvm;

/**
 * 循环引用 GC
 * -XX:+PrintGCDetails
 */
public class LoopReferenceGC {
    // 5M
    public byte[] bytes = new byte[5 * 1024 * 1204];
    LoopReferenceGC ref = null;

    public static void main(String[] args) {
        LoopReferenceGC obj1 = new LoopReferenceGC();
        LoopReferenceGC obj2 = new LoopReferenceGC();
        obj1.ref = obj2;
        obj2.ref = obj1;
        obj1 = null;
        obj2 = null;
        // PSYoungGen      total 76288K, used 1966K
        // 说明循环引用已回收
        System.gc();
    }

}
