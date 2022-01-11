package designMode.singleton;

import java.util.concurrent.TimeUnit;

/**
 * 懒汉（线程不安全）
 * @author caoyang
 */
public class SingletonLazyInsecurity {
    private static SingletonLazyInsecurity instance;
    private SingletonLazyInsecurity() {}

    public static SingletonLazyInsecurity getInstance(){
        if(instance == null){
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            instance = new SingletonLazyInsecurity();
        }
        return instance;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                System.out.println(SingletonLazyInsecurity.getInstance());
            }).start();
        }
    }
}
