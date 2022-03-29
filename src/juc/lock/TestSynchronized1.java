package src.juc.lock;

/**
 * synchronized可重入锁
 * @author caoyang
 */
public class TestSynchronized1 {
    public static void main(String[] args) {
        synchronized (TestSynchronized1.class){
            System.out.println("first get lock");
            synchronized (TestSynchronized1.class){
                System.out.println("get lock again");
            }
        }
    }
}
