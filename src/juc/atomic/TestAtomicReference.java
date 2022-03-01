package src.juc.atomic;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author caoyang
 */
public class TestAtomicReference {
    AtomicReference<Thread> owner = new AtomicReference<>();

    /**
     * 利用自旋实现一个不可重入锁
     */
    public void lock(){
        Thread thread = Thread.currentThread();
        while (true){
            if(owner.compareAndSet(null, thread)){
                System.out.println("locked");
                return;
            }
            System.out.println("cannot get lock...");
        }
    }
    public void unlock(){
        Thread thread = Thread.currentThread();
        owner.compareAndSet(thread, null);
        System.out.println("unlocked");
    }

    public static void main(String[] args) {
        TestAtomicReference test = new TestAtomicReference();
        test.lock();
        test.lock();
    }
}
