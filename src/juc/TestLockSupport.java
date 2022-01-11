package src.juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author caoyang
 */
public class TestLockSupport {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                if(i == 4){
                    // 停车
                    LockSupport.park();
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(i);
            }
        }, "childThead");
        t.start();
        try {
            TimeUnit.SECONDS.sleep(10);
            System.out.println(String.format("after 10 seconds, %s go on!", t.getName()));
            // 继续开车！
            LockSupport.unpark(t);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
