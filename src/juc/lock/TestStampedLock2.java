package src.juc.lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;

public class TestStampedLock2 {

    private int balance;

    private StampedLock lock = new StampedLock();
    public TestStampedLock2() {
        balance=10;
    }

    public void conditionReadWrite (int value) {
        // 首先判断balance的值是否符合更新的条件
        long stamp = lock.readLock();
        while (balance > 0) {
            long writeStamp = lock.tryConvertToWriteLock(stamp);
            // 成功转换成为写锁
            if(writeStamp != 0) {
                stamp = writeStamp;
                balance += value;
                break;
            } else {
                // 没有转换成写锁，这里需要首先释放读锁，然后再拿到写锁
                lock.unlockRead(stamp);
                // 获取写锁
                stamp = lock.writeLock();
            }
        }
        lock.unlock(stamp);
    }

    public void optimisticRead() {
        long stamp = lock.tryOptimisticRead();
        int c = balance;
        // 这里可能会出现了写操作，因此要进行判断
        if(!lock.validate(stamp)) {
            // 要重新读取
            stamp = lock.readLock();
            try{
                c = balance;
            }
            finally{
                lock.unlockRead(stamp);
            }
        }
        System.out.println(Thread.currentThread().getName() +"读取的值为: "+c);
    }

    public void read () {
        long stamp = lock.readLock();
        lock.tryOptimisticRead();
        int c = balance;
        System.out.println(Thread.currentThread().getName() +"读取的值为: "+c);
        // ...
        lock.unlockRead(stamp);
    }

    public void write(int value) {
        long stamp = lock.writeLock();
        balance += value;
        lock.unlockWrite(stamp);
    }

    public static void main(String[] args) {
        TestStampedLock2 test = new TestStampedLock2();
        new Thread(() -> {
            while(true){
                test.read();
                test.optimisticRead();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1").start();
        new Thread(() -> {
            while(true){
                test.write(2);
                test.conditionReadWrite(3);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t2").start();
    }

}

