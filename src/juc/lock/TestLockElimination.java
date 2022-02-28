package src.juc.lock;

/**
 * 锁消除
 * @author caoyang
 */
public class TestLockElimination {

    public void add(String str1, String str2){
        StringBuffer sb = new StringBuffer();
        // 线程私有，不被共享，jvm 会消除StringBuffer的锁
        sb.append(str1).append(str2);
    }

    public static void main(String[] args) {

    }
}
