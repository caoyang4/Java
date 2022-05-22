package src.jvm;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class LockByteCodeTest {
    ReentrantLock lock = new ReentrantLock();
    volatile boolean flag = false;
    int i = 0;


    /**
     *  0 aload_0
     *  1 dup
     *  2 astore_1
     *  3 monitorenter
     *  4 aload_1
     *  5 monitorexit
     *  6 goto 14 (+8)
     *  9 astore_2
     * 10 aload_1
     * 11 monitorexit
     * 12 aload_2
     * 13 athrow
     * 14 return
     */
    public void syncMethod(){
        // monitorenter 可重入
        synchronized (this){
            i++;
        }
    }

    /**
     *  0 aload_0
     *  1 getfield #4 <src/jvm/ByteCodeTest.lock : Ljava/util/concurrent/locks/ReentrantLock;>
     *  4 invokevirtual #5 <java/util/concurrent/locks/ReentrantLock.lock : ()V>
     *  7 aload_0
     *  8 getfield #4 <src/jvm/ByteCodeTest.lock : Ljava/util/concurrent/locks/ReentrantLock;>
     * 11 invokevirtual #6 <java/util/concurrent/locks/ReentrantLock.unlock : ()V>
     * 14 return
     */
    public void lockMethod(){
        lock.lock();
        try {

        } finally {
            lock.unlock();
        }
    }

    public int volatileMethod(){
        int j = 1;
        i += j;
        flag = true;
        return i;
    }

    public int exceptionMethod(){
        int i = 0;
        try {
            Thread.sleep(1000);
            return i;
        } catch (InterruptedException | OutOfMemoryError e) {
            e.printStackTrace();
            i = 1;
            return i;
        } finally {
            i = 2;
        }

    }

    public void loopMethod(int i){
        while (i < 100){
            System.out.println(i++);
        }
    }
}
