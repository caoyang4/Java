package src.juc.lock;

/**
 * @author caoyang
 */
public class TestWaitNotify1 {

    public static void main(String[] args) throws InterruptedException {
        TestWaitNotify1Thread thread = new TestWaitNotify1Thread();
        synchronized (thread){
            thread.start();
            Thread.sleep(000);
            System.out.println("begin to wait");
            // Thread.sleep()不会释放占有的锁，
            // Object.wait()会释放占有的锁
            thread.wait();
            System.out.println("end to wait");
        }
    }

}

class TestWaitNotify1Thread extends Thread{
    @Override
    public void run() {
        synchronized (this){
            System.out.println("begin to notify");
            notify();
            System.out.println("end to notify");
        }
    }
}
