package src.jvm;

/**
 * 栈上分配对象
 * -Xms1g -Xmx1g -XX:-DoEscapeAnalysis -XX:+PrintGCDetails
 * 关闭逃逸分析：-XX:-DoEscapeAnalysis
 *   5618ms 发生gc
 * 开启逃逸分析 -XX:+DoEscapeAnalysis
 *   12ms 没有发生gc
 */
public class StackAllocation {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            alloc();
        }
        System.out.println((System.currentTimeMillis()-start) + "ms");
    }
    private static void alloc(){
        // 未发生逃逸
        User user = new User();
    }
    static class User{
    }
}
