package src.juc.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractQueuedSynchronizer
 * AQS使用了模板方法模式，自定义同步器时需要重写下面几个AQS提供的模板方法：
 * isHeldExclusively()//该线程是否正在独占资源。只有用到condition才需要去实现它。
 * tryAcquire(int)//独占方式。尝试获取资源，成功则返回true，失败则返回false。
 * tryRelease(int)//独占方式。尝试释放资源，成功则返回true，失败则返回false。
 * tryAcquireShared(int)//共享方式。尝试获取资源。负数表示失败；0表示成功，但没有剩余可用资源；正数表示成功，且有剩余资源。
 * tryReleaseShared(int)//共享方式。尝试释放资源，成功则返回true，失败则返回false。
 *
 * AQS与Condition结合实现生产者消费者模型
 * @author caoyang
 */
public class TestAQS {
    public static void main(String[] args) {
        Depot depot = new Depot(500);
        // 先生产 500 个，到达容量限制，通知 Consumer 消费，线程结束
        new Producer(depot).produce(500);
        // 到达容量，不再生产，阻塞，释放锁，Consumer消费之后，获取锁，再生产，再通知Consumer消费
        new Producer(depot).produce(200);
        // 消费 500 个，容量变为 0，通知 Producer 生产，线程结束
        new Consumer(depot).consume(500);
        // 发现容量变为 0，阻塞，释放锁，通知 Producer 生产，获取锁，继续消费
        new Consumer(depot).consume(200);
    }
}

/**
 * 生产和消费的仓库
 */
class Depot{
    /**
     * 仓库大小
     */
    private int size;
    /**
     * 仓库容量
     */
    private int capacity;
    private Lock lock;
    private Condition produce;
    private Condition consume;

    public Depot(int capacity) {
        this.capacity = capacity;
        size = 0;
        lock = new ReentrantLock(false);
        produce = lock.newCondition();
        consume = lock.newCondition();
    }

    public void produceTask(int produceNum){
        lock.lock();
        try {
            int left = produceNum;
            String threadName = Thread.currentThread().getName();
            while (left > 0){
                while (size >= capacity){
                    System.out.println(threadName + " begin to await...");
                    produce.await();
                    System.out.println(threadName + " end to await...");
                }
                int inc = (left + size) > capacity ? capacity - left : left;
                left -= inc;
                size += inc;
                System.out.println(threadName + " produce " + inc + ", size = " + size);
                consume.signal();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void consumeTask(int consumeNum){
        lock.lock();
        try {
            String threadName = Thread.currentThread().getName();
            int left = consumeNum;
            while (left > 0){
                while (size <= 0){
                    System.out.println(threadName + " begin to await...");
                    consume.await();
                    System.out.println(threadName + " end to await...");
                }
                int inc = (size - left) > 0 ? left : size;
                left -= inc;
                size -= inc;
                System.out.println(threadName + " consume " + inc + ", size = " + size);
                produce.signal();
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

/**
 * 生产者
 */
class Producer{
    private Depot depot;
    public Producer(Depot depot) {
        this.depot = depot;
    }
    public void produce(int num){
        new Thread(() -> depot.produceTask(num), "ProduceThread").start();
    }
}

class Consumer{
    private Depot depot;
    public Consumer(Depot depot) {
        this.depot = depot;
    }
    public void consume(int num){
        new Thread(() -> depot.consumeTask(num), "ConsumeThread").start();
    }
}
