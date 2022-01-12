package src.designPattern.singleton;

import java.util.concurrent.TimeUnit;

/**
 * 懒汉 线程安全
 * 效率低
 * @author caoyang
 */
public class SingleTonLazySecurity {
    private static SingleTonLazySecurity instance;
    private SingleTonLazySecurity() {}

    public static synchronized SingleTonLazySecurity getInstance(){
        if(instance == null){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instance = new SingleTonLazySecurity();
        }
        return instance;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                System.out.println(SingleTonLazySecurity.getInstance());
            }).start();
        }
    }
}
