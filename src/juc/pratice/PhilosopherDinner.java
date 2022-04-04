package src.juc.pratice;

import lombok.extern.slf4j.Slf4j;
import src.juc.JucUtils;

/**
 * 死锁——哲学家就餐问题
 * @author caoyang
 */
public class PhilosopherDinner {
    public static void main(String[] args) {
        Chopstick stick1 = new Chopstick("金");
        Chopstick stick2 = new Chopstick("木");
        Chopstick stick3 = new Chopstick("水");
        Chopstick stick4 = new Chopstick("火");
        Chopstick stick5 = new Chopstick("土");
        new Philosopher("孔子", stick1, stick2).start();
        new Philosopher("老子", stick2, stick3).start();
        new Philosopher("墨子", stick3, stick4).start();
        new Philosopher("苏格拉底", stick4, stick5).start();
        // 顺序加锁可解决死锁
        /*new Philosopher("亚里士多德", stick1, stick5).start();*/
        new Philosopher("亚里士多德", stick5, stick1).start();
    }
}
final class Chopstick{
    private String name;

    public Chopstick(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

@Slf4j(topic = "Philosopher")
class Philosopher extends Thread{
    private String name;
    private Chopstick left;
    private Chopstick right;

    public Philosopher(String name, Chopstick left, Chopstick right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    private void eat(){
        log.info("{}获得筷子【{}】和【{}】，可以吃饭", name, left.getName(), right.getName());
    }

    @Override
    public void run() {
        while (true) {
            synchronized (left){
                synchronized (right){
                    JucUtils.sleepMillSeconds(200);
                    eat();
                }
            }
        }
    }
}
