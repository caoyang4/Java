package src.juc.lock;

/**
 * 锁粗化
 * @author caoyang
 */
public class TestLockCoarsening {
    public void add(String str){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 100; i++) {
            // jvm 检测到一连串对同一个对象加锁，会将锁范围粗化到这一连串动作的外部，如对for循环体加锁
            sb.append(str);
        }
    }
    public static void main(String[] args) {

    }
}
