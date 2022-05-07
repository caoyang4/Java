package src.juc.executors.thread.threadProblem;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

import java.util.concurrent.*;

/**
 * 线程饥饿问题
 * 活锁是指线程一直处于运行状态,但是任务却一直无法进展的一种活性故障,即产生活锁的线程一直做无用功
 *
 * 在Java中，下面三个常见的原因会导致线程饥饿：
 *  1、高优先级线程吞噬所有的低优先级线程的CPU时间。
 *  2、线程被永久堵塞在一个等待进入同步块的状态，因为其他线程总是能在它之前持续地对该同步块进行访问。
 *  3、线程在等待一个本身(在其上调用wait())也处于永久等待完成的对象，因为其他线程总是被持续地获得唤醒。
 * @author caoyang
 */
@Slf4j(topic = "TestStarvation")
public class TestStarvation {
    public static void main(String[] args) {
        //创建只有一个线程的线程池
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        //向线程池中添加取数据任务与添加数据的任务
        executorService.submit(new TakeDataTask());
        // 取不到数据，该任务会一直阻塞到这里，获取不到资源
        executorService.submit(new AddDataTask());
        executorService.shutdown();
    }

    //创建一个阻塞队列
    private static final BlockingQueue<Integer> QUEUE = new ArrayBlockingQueue<>(10);
    //定义一个向阻塞队列中添加数据的任务
    private static class AddDataTask implements Runnable{
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getId() +  " 编号的线程执行添加数据的任务");
            try {
                QUEUE.put(123);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //定义从队列中取数据的任务
    private static class  TakeDataTask implements Runnable{
        @Override
        public void run() {
            System.out.println( Thread.currentThread().getId()  + " 编号的线程执行取数据的任务");
            try {
                int data = QUEUE.take();
                System.out.println(Thread.currentThread().getId() + "："+data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
