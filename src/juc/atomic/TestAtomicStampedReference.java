package src.juc.atomic;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 解决 ABA 问题
 * @author caoyang
 */
public class TestAtomicStampedReference {
    private static AtomicStampedReference<Integer> atomicStampedRef = new AtomicStampedReference<>(1, 0);
    public static void main(String[] args) {
        Thread main = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() +",初始值 a = " + atomicStampedRef.getReference());
            int stamp = atomicStampedRef.getStamp(); //获取当前标识别
            try {
                Thread.sleep(1000); //等待1秒 ，以便让干扰线程执行
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean isCASSuccess = atomicStampedRef.compareAndSet(1,2,stamp,stamp +1);  //此时expectedReference未发生改变，但是stamp已经被修改了,所以CAS失败
            System.out.println(Thread.currentThread().getName() +", CAS操作结果: " + isCASSuccess);
        },"主操作线程");

        Thread other = new Thread(() -> {
            // 确保 thread-main 优先执行
            Thread.yield();
            atomicStampedRef.compareAndSet(1,2,atomicStampedRef.getStamp(),atomicStampedRef.getStamp() +1);
            System.out.println(Thread.currentThread().getName() +",【increment】 ,值 = "+ atomicStampedRef.getReference());
            atomicStampedRef.compareAndSet(2,1,atomicStampedRef.getStamp(),atomicStampedRef.getStamp() +1);
            System.out.println(Thread.currentThread().getName() +",【decrement】 ,值 = "+ atomicStampedRef.getReference());
        },"干扰线程");

        main.start();
        other.start();
    }
}
