package src.juc.lock;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import static java.lang.System.*;

/**
 * Phaser
 * @author caoyang
 */
public class TestPhaser {
    final static int BOUND = 1000;
    final static int GUEST_NUM = 5;
    static Random random = new Random();
    static MarriagePhaser phaser = new MarriagePhaser();


    public static void main(String[] args) {
        phaser.bulkRegister(GUEST_NUM+2);
        for (int i=0; i<GUEST_NUM; i++){
            int j = i+1;
            new Thread(new Person("宾客" + j)).start();
        }
        new Thread(new Person("新郎")).start();
        new Thread(new Person("新娘")).start();
    }

    public static void timeSleep(int millSec){
        try {
            TimeUnit.MILLISECONDS.sleep(millSec);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Person implements Runnable{
        String name;
        public Person(String name) {
            this.name = name;
        }
        public void arrive(){
            timeSleep(random.nextInt(BOUND));
            out.printf("%s 到达现场！\n", name);
            // 第1层栅栏
            phaser.arriveAndAwaitAdvance();
        }

        public void eat(){
            timeSleep(random.nextInt(BOUND));
            out.printf("%s 开始宴席！\n", name);
            // 第2层栅栏
            phaser.arriveAndAwaitAdvance();
        }

        public void leave(){
            timeSleep(random.nextInt(BOUND));
            out.printf("%s 离开！\n", name);
            // 第3层栅栏
            phaser.arriveAndAwaitAdvance();
        }

        public void hug(){
            if("新郎".equals(name) || "新娘".equals(name)){
                timeSleep(random.nextInt(BOUND));
                out.printf("%s 交换戒指！\n", name);
                // 第4层栅栏
                phaser.arriveAndAwaitAdvance();
            } else {
                // 宾客各回各家
                phaser.arriveAndDeregister();
            }
        }

        @Override
        public void run() {
            arrive();
            eat();
            leave();
            hug();
        }
    }

    static class MarriagePhaser extends Phaser{
        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            switch (phase){
                case 0:
                    out.println("所有人到齐了！" + registeredParties);
                    out.println("*********************************");
                    return false;
                case 1:
                    out.println("所有人吃完了！" + registeredParties);
                    out.println("*********************************");
                    return false;
                case 2:
                    out.println("所有人离开了！" + registeredParties);
                    out.println("*********************************");
                    return false;
                case 3:
                    out.println("撒花，婚礼结束，新郎新娘礼成！" + registeredParties);
                    out.println("66666666666666666666666666666666666");
                    return true;
                default:
                    return true;
            }
        }
    }

}
