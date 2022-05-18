package src.juc;

import java.util.concurrent.TimeUnit;

/**
 * @author caoyang
 */
public final class JucUtils {
    private JucUtils() {
    }

    public static void threadSleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void sleepSeconds(long seconds){
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void sleepMillSeconds(long millSeconds){
        try {
            TimeUnit.MILLISECONDS.sleep(millSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
