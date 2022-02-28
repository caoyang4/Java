package src.juc.lock;

import java.util.concurrent.locks.LockSupport;

/**
 * @author caoyang
 */
public class TestLockSupport2 {
    public static void main(String[] args) throws InterruptedException {
        ThreadA thread = new ThreadA(Thread.currentThread());
        thread.start();
        System.out.println("start to park");
        LockSupport.park("TestLockSupport");
        System.out.println("end to park");
    }
}

class ThreadA extends Thread{
    private Object object;
    public ThreadA(Object obj){
        this.object = obj;
    }

    @Override
    public void run() {
        try {
            System.out.println("begin to unpack");
            // 阻塞，先执行 park，执行第一个 setBlocker
            Thread.sleep(1000);
            Thread thread = (Thread) object;
            System.out.println("first Blocker info " + LockSupport.getBlocker(thread));
            LockSupport.unpark(thread);
            // 阻塞，执行 park 的第二个 setBlocker
            Thread.sleep(1000);
            System.out.println("second Blocker info " + LockSupport.getBlocker(thread));
            System.out.println("end to unpack");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
