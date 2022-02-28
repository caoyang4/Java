package src.juc.lock;

public class TestStringLock {

    public void method() {
        // synchronized (new String("字符串常量")) 锁对象不是同一个锁，并不会生效
        // "字符串常量" 锁生效，但是不建议，如果其他地方有用到该变量，可能导致用于获取不到锁
        synchronized ("字符串常量") {
            try {
                while(true){
                    System.out.println("当前线程 : "  + Thread.currentThread().getName() + "开始");
                    Thread.sleep(1000);
                    System.out.println("当前线程 : "  + Thread.currentThread().getName() + "结束");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        final TestStringLock stringLock = new TestStringLock();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                stringLock.method();
            }
        },"t1");

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                stringLock.method();
            }
        },"t2");

        t1.start();
        t2.start();

    }
}

