package src.designMode.singleton;

import java.util.concurrent.TimeUnit;

/**
 * DCL
 *
 * @author caoyang
 */
public class SingleTonDCL {
    // volatile 防止多线程下初始化不完全
    private volatile static SingleTonDCL instance;
    private SingleTonDCL() {}

    public static SingleTonDCL getInstance(){
        if (instance == null){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (SingleTonDCL.class){
                if (instance == null){
                    instance = new SingleTonDCL();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                System.out.println(SingleTonDCL.getInstance());
            }).start();
        }
    }

}
