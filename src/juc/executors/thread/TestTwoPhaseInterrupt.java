package src.juc.executors.thread;

import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public class TestTwoPhaseInterrupt {
    public Thread monitor;
    private volatile boolean start = false;

    public void start(){
        if(start){
            return;
        }
        synchronized (this) {
            if (start) {
                return;
            }
            start = true;
        }
        monitor = new Thread(() -> {
            while (true) {
                if(monitor.isInterrupted()){
                    doAfterInterrupt();
                    break;
                }
                try {
                    doMonitor();
                    // sleep 会清楚中断标记
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    monitor.interrupt();
                }
            }
        });
        monitor.start();
        System.out.println("创建监控线程...");
    }

    public void stop(){
        // 中断线程
        monitor.interrupt();
    }

    private void doAfterInterrupt(){
        System.out.println("欲知后事如何，且听下回分解...");
    }
    private void doMonitor(){
        System.out.println("当你凝视深渊的时候，深渊也在凝视你...");
    }

    public static void main(String[] args) throws InterruptedException {
        TestTwoPhaseInterrupt test = new TestTwoPhaseInterrupt();
        test.start();
        test.start();
        TimeUnit.SECONDS.sleep(5);
        test.stop();
    }

}

