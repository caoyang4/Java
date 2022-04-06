package src.juc.executors.threadPool.SelfRealizedPool;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 简单实现线程池
 * @author caoyang
 */
@Slf4j(topic = "SelfExecutor")
public class SelfExecutor {
    private SelfBlockingQueue<Runnable> queue;
    private final Set<Worker> workers;
    private int coreSize;
    private long timeout;
    private TimeUnit unit;
    private SelfRejectPolicy<Runnable> rejectPolicy;

    public SelfExecutor(int coreSize, int queueCapacity, long timeout, TimeUnit unit, SelfRejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.unit = unit;
        queue = new SelfBlockingQueue<>(queueCapacity);
        workers = new HashSet<>();
        this.rejectPolicy = rejectPolicy;
    }

    public void execute(Runnable task){
        synchronized (workers){
            if (workers.size() < coreSize){
                Worker worker = new Worker(task);
                workers.add(worker);
                log.info("添加任务: {}", worker);
                worker.start();
            } else {
                // queue.put(task);
                // 死等
                // 超时等待
                // 抛出异常
                queue.tryPut(rejectPolicy, task);
            }
        }
    }

    private final class Worker extends Thread{
        Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            while (task != null || (task = queue.poll(timeout, unit)) != null){
                try {
                    log.info("执行任务 {}", task);
                    task.run();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            // 退出循环，将 worker 拿掉
            synchronized (workers) {
                workers.remove(this);
                log.info("移除Worker: {}", this);
            }
        }

        @Override
        public String toString() {
            return "Worker{" + "task=" + task + '}';
        }
    }
}
