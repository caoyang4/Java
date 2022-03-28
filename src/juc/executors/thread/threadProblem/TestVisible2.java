package src.juc.executors.thread.threadProblem;

public class TestVisible2 {
    private static /*volatile*/ boolean ready = false;
    private static int number;
    private static class ReaderThread extends Thread{
        @Override
        public void run() {
            while (!ready){
                System.out.println("未检测到 ready 已变为 false");
                Thread.yield();
            }
            System.out.println("ready==true被写到主存，检测到ready已变为true，number:" + number);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new ReaderThread().start();

        new Thread(() -> {
            number = 1101;
            ready = true;
        }).start();
    }
}
