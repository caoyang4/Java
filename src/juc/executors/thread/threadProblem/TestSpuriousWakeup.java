package src.juc.executors.thread.threadProblem;

/**
 * 线程虚假唤醒验证
 *
 * 为什么会产生虚假唤醒？
 *  1、生产者唤醒了所有处于阻塞队列中的线程，我们希望的是生产者A唤醒的应该是两个消费者，而不是唤醒了生产者B
 *  2、我们都知道，wait方法的作用是将线程停止执行并送入到阻塞队列中，但是wait方法还有一个操作就是释放锁。
 *    因此当生产者A执行wait方法时，该线程就会把它持有的对象锁释放，
 *    这样生产者B就可以拿到锁进入synchronized修饰的push方法中，即使它被卡在if判断，但被唤醒后它就会又添加一个产品了
 * @author caoyang
 */
public class TestSpuriousWakeup {
    public static void main(String[] args) {
        Product product = new Product();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.push();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "生产者A").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.pop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "消费者A").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.push();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "生产者B").start();
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    product.pop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "消费者B").start();
    }
    static class Product {
        private int product = 0;

        /**
         * 解决虚假唤醒，将 if判断 改成 while循环 即可
         */
        public synchronized void push() throws InterruptedException {
            System.out.println(Thread.currentThread().getName() + "进入push方法");
            if (product > 0) {
                this.wait();
            }
            product++;
            System.out.println(Thread.currentThread().getName() + "添加产品，剩余" + product + "件产品");
            this.notifyAll();
        }

        public synchronized void pop() throws InterruptedException {
            System.out.println(Thread.currentThread().getName() + "进入pop方法");
            if (product == 0) {
                this.wait();
            }
            product--;
            System.out.println(Thread.currentThread().getName() + "使用产品，剩余" + product + "件产品");
            this.notifyAll();
        }
    }
}
