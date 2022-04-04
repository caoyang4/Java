package src.juc.executors.thread.threadProblem;

import src.juc.JucUtils;

public class TestVisible2 {
    private static /*volatile*/ boolean ready = true;
    private static int number;

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            while (ready){
                // println打印之后，会flushBuffer，刷新缓存,会重新读ready的值，即此处有println，会使t1读到主存ready为true的值，程序不会无限循环
                // 若此处没有println，会无限循环
                System.out.println("未检测到 ready 已变为 false");
            }
            System.out.println("ready==false被写到主存，检测到ready已变为false，number:" + number);
        }).start();

        new Thread(() -> {
            JucUtils.sleepSeconds(1);
            number = 1101;
            ready = false;
        }).start();
    }
}
