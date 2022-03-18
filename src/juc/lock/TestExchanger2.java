package src.juc.lock;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public class TestExchanger2 {
    public static void main(String[] args) throws InterruptedException {
        Exchanger<Integer> exchanger = new Exchanger<>();
        new Producer("Producer", exchanger).start();
        new Consumer("Consumer", exchanger).start();
        TimeUnit.SECONDS.sleep(20);
        System.exit(-1);
    }

    static class Producer extends Thread{
        Exchanger<Integer> exchanger;
        static int data = 0;

        public Producer(String name, Exchanger<Integer> exchanger) {
            super(name);
            this.exchanger = exchanger;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= 5; i++) {
                    TimeUnit.SECONDS.sleep(1);
                    data = i;
                    System.out.println(getName()+" 交换前:" + data);
                    data = exchanger.exchange(data);
                    System.out.println(getName()+" 交换后:" + data);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Consumer extends Thread{
        Exchanger<Integer> exchanger;
        static int data = 0;

        public Consumer(String name, Exchanger<Integer> exchanger) {
            super(name);
            this.exchanger = exchanger;
        }
        @Override
        public void run() {
            for (;;) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    data = 666;
                    System.out.println(getName()+" 交换前:" + data);
                    data = exchanger.exchange(data);
                    System.out.println(getName()+" 交换后:" + data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
