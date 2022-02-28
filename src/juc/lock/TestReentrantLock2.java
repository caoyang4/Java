package src.juc.lock;

/**
 *
 * @author caoyang
 */
public class TestReentrantLock2 {

    public void myReentrantLockTest(MyReentrantLock lock) throws InterruptedException {
        lock.lock();
        myReentrantLockWork(lock);
        lock.unlock();
    }
    public void myReentrantLockWork(MyReentrantLock lock) throws InterruptedException {
        lock.lock();
        System.out.println(Thread.currentThread().getName() + " MyReentrantLockWork");
        lock.unlock();
    }

    public void myUnReentrantLockTest(MyUnReentrantLock lock) throws InterruptedException {
        lock.lock();
        myUnReentrantLockWork(lock);
        lock.unlock();
    }
    public void myUnReentrantLockWork(MyUnReentrantLock lock) throws InterruptedException {
        lock.lock();
        System.out.println(Thread.currentThread().getName() + " MyUnReentrantLockWork");
        lock.unlock();
    }


    public static void main(String[] args) throws InterruptedException {
        TestReentrantLock2 test = new TestReentrantLock2();
        MyReentrantLock rlLock = new MyReentrantLock();
        MyUnReentrantLock unRlLock = new MyUnReentrantLock();
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    test.myReentrantLockTest(rlLock);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "ReentrantLockThread1");

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    test.myReentrantLockTest(rlLock);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "ReentrantLockThread2");

        thread1.start();
        thread2.start();

        /* 非重入锁测试
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    test.myUnReentrantLockTest(unRlLock);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "UnReentrantLockThread1").start();

        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                try {
                    test.myUnReentrantLockTest(unRlLock);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "UnReentrantLockThread2").start();
        */



    }
}

/**
 * 实现可重入锁
 */
class MyReentrantLock{
    private boolean isLocked = false;
    private Thread lockedThread = null;
    private int blockedCount = 0;

    public boolean isLocked() {
        return isLocked;
    }

    public int getBlockedCount() {
        return blockedCount;
    }

    public synchronized void lock() throws InterruptedException {
        Thread thread = Thread.currentThread();
        // 同一个线程不用阻塞
        while (isLocked && lockedThread != thread){
            System.out.println(thread.getName() + " wait");
            wait();
        }
        isLocked = true;
        lockedThread = thread;
        blockedCount++;
        if(blockedCount < 0){
            throw new Error("Maximum lock count exceeded");
        }
    }

    public synchronized void unlock(){
        Thread thread = Thread.currentThread();
        if(thread == this.lockedThread){
            blockedCount--;
            if(blockedCount == 0){
                isLocked = false;
                notify();
            }
        }
    }

}

/**
 * 实现不可重入锁
 */
class MyUnReentrantLock{
    private boolean isLocked = false;
    public synchronized void lock() throws InterruptedException {
        while (isLocked){
            wait();
        }
        isLocked = true;
    }

    public synchronized void unlock(){
        if (isLocked){
            isLocked = false;
            notify();
        }
    }

    public boolean isLocked() {
        return isLocked;
    }
}