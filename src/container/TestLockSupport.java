package src.container;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author caoyang
 */
public class TestLockSupport {
    List<Object> objects = new ArrayList<>();
    static Thread t2 = null;
    public static void main(String[] args) {
        TestLockSupport test = new TestLockSupport();
        Thread t1 = new Thread(() -> {
            System.out.println("线程t1启动...");
            for (int i = 0; i < 10; i++) {
                System.out.println("objects添加object"+i);
                test.objects.add(new Object());
                if(i == 4){
                    LockSupport.unpark(t2);
                    LockSupport.park();
                }
            }
            System.out.println("线程t1结束...");
        });

       t2 = new Thread(() -> {
           System.out.println("线程t2启动...");
           LockSupport.park();
           LockSupport.unpark(t1);
           System.out.println("线程t2结束...");
        });

        t2.start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t1.start();
    }
}
