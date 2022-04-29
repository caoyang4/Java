package src.juc.executors.thread;

import src.juc.JucUtils;

import java.util.concurrent.TimeUnit;

public class TestInterrupt3 {
    public static void main(String[] args) throws Exception {
        Thread t = new MyThread();
        t.start();
        // 如果不能看到处理过程中被中断的情形，可以启用这句再看看效果
        JucUtils.sleepSeconds(1);
        t.interrupt();
        System.out.println("已调用线程的interrupt方法");
    }
    static class MyThread extends Thread {
        public void run() {
            int num;
            try {
                num = longTimeRunningNonInterruptMethod(2, 0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("长时间任务运行结束, num=" + num);
            System.out.println("线程的中断状态:" + Thread.interrupted());
        }
        private static int longTimeRunningNonInterruptMethod(int count, int initNum) throws InterruptedException {
            if(interrupted()) {
                throw new InterruptedException("正式处理前线程已经被请求中断");
            }
            JucUtils.sleepSeconds(1);
            for(int i=0; i<count; i++) {
                for(int j=0; j < 10; j++) {
                    initNum ++;
                }
                //假如这就是一个合适的地方
                if(interrupted()) {
                    //回滚数据，清理操作等
                    throw new InterruptedException("线程正在处理过程中被中断");
                }
            }
            return initNum;
        }
    }
}
