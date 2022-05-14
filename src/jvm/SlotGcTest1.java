package src.jvm;

/**
 * 局部变量表垃圾回收
 * -verbose:gc -XX:+PrintCommandLineFlags
 * Java8 中，默认搭配Parallel（young gc）+ Parallel Old (Major gc) 使用，且二者互相激活
 * 支持自适应调节
 * @author caoyang
 */
public class SlotGcTest1 {
    public static void main(String[] args) {
        byte[] holder = new byte[64 * 1024 * 1024];
        // [GC (System.gc())  70786K->66408K(251392K), 0.0036547 secs]
        // [Full GC (System.gc())  66408K->66198K(251392K), 0.0150258 secs] 没有回收掉
        // 执行 gc 时，holder 还处于作用域内
        System.gc();
    }
}
