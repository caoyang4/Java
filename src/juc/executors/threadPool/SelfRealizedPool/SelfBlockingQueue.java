package src.juc.executors.threadPool.SelfRealizedPool;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 实现简单的阻塞队列
 * @author caoyang
 */
@Slf4j(topic = "SelfBlockingQueue")
public class SelfBlockingQueue<T> {
    private Deque<T> deque;
    private int capacity;

    private ReentrantLock lock;
    private Condition fullCondition;
    private Condition emptyCondition;

    public SelfBlockingQueue(int capacity) {
        this.capacity = capacity;
        deque = new ArrayDeque<>();
        lock = new ReentrantLock();
        fullCondition = lock.newCondition();
        emptyCondition = lock.newCondition();
    }

    public T take(){
        lock.lock();
        try {
            while (deque.isEmpty()){
                try {
                    log.info("emptyQueue, wait to produce");
                    emptyCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T result = deque.removeFirst();
            fullCondition.signalAll();
            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 超时获取对象
     */
    public T poll(long timeout, TimeUnit unit){
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (deque.isEmpty()){
                try {
                    if (nanos <= 0){
                        log.info("poll timeout...");
                        return null;
                    }
                    nanos = emptyCondition.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T result = deque.removeFirst();
            fullCondition.signalAll();
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void put(T element){
        lock.lock();
        try {
            while (deque.size() == capacity){
                try {
                    log.info("队列已满, 等待消费");
                    fullCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            deque.add(element);
            log.info("添加队列: {}", element);
            emptyCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean offer(T element, long timeout, TimeUnit unit){
        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (deque.size() == capacity){
                try {
                    if (nanos <= 0){
                        log.info("offer timeout...");
                        return false;
                    }
                    nanos = fullCondition.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            deque.add(element);
            log.info("添加队列: {}", element);
            emptyCondition.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void tryPut(SelfRejectPolicy reject, T element){
        lock.lock();
        try {
            if (deque.size() == capacity) {
                reject.reject(this, element);
            } else {
                deque.add(element);
                log.info("添加队列: {}", element);
            }
        } finally {
            lock.unlock();
        }
    }

    public int size(){
        lock.lock();
        try {
            return deque.size();
        } finally {
            lock.unlock();
        }
    }

}
