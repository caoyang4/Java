package src.jvm;

/**
 * 标量替换
 *  -Xms1g -Xmx1g -XX:+DoEscapeAnalysis -XX:-EliminateAllocations -XX:+PrintGCDetails
 * 关闭标量替换 -XX:-EliminateAllocations
 *  8878ms 发生GC
 * 打开标量替换 -XX:+EliminateAllocations
 *  9ms，没有发生GC
 */
public class ScalarReplace {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            alloc();
        }
        System.out.println((System.currentTimeMillis()-start) + "ms");
    }
    private static void alloc(){
        User user = new User();
        user.age = 18;
        user.name = "zhangzhe";
    }
    static class User{
        int age;
        String name;
    }
}
