package src.juc.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在旅游过程中，有可能很凑巧遇到几个朋友，然后他们听说你们在旅游，所以想要加入一起继续接下来的旅游。
 * 也有可能，在旅游过程中，突然其中有某几个人临时有事，想退出这次旅游了。
 * 使用CyclicBarrier实现不了，用Phaser就可实现这个功能
 * @author caoyang
 */
public class TestPhaser4 {
    final static Map<Integer, String> POINT_MAP = new HashMap<>();
    public static String[] persons;
    static {
        persons = new String[]{
                "james",
                "kobe",
                "wade",
                "luffy",
                "paul",
                "young"
        };
        POINT_MAP.put(1, "机场");
        POINT_MAP.put(2, "酒店");
        POINT_MAP.put(3, "景点1");
        POINT_MAP.put(4, "景点2");
        POINT_MAP.put(5, "景点3");
        POINT_MAP.put(6, "回家");
    }
    public static void main(String[] args) {
        Phaser phaser = new TestPhaser4.SubPhaser(persons.length);
        for (String person : persons) {
            new Thread(new TestPhaser4.Tourism2(phaser), person).start();
        }
    }
    static class Tourism2  implements Runnable{
        Phaser phaser;
        AtomicInteger friendCount = new AtomicInteger();
        Random random;

        public Tourism2(Phaser phaser) {
            this.phaser = phaser;
            random = new Random();
        }

        /**
         * 返程回家
         */
        private boolean goToEndPoint() {
            return goToPoint("飞机场，准备登机回家");
        }

        /**
         * 到达旅游点3
         */
        private boolean goToTourismPoint3() {
            return goToPoint("旅游点3");
        }

        /**
         * 到达旅游点2
         */
        private boolean goToTourismPoint2() {
            return goToPoint("旅游点2");
        }

        /**
         * 到达旅游点1
         */
        private boolean goToTourismPoint1() {
            return goToPoint("旅游点1");
        }

        /**
         * 入住酒店
         */
        private boolean goToHotel() {
            return goToPoint("酒店");
        }

        /**
         * 出发点集合
         */
        private boolean goToStartingPoint() {
            return goToPoint("出发点");
        }

        private int getRandomTime() throws InterruptedException {
            int time = this.random.nextInt(400) + 100;
            Thread.sleep(time);
            return time;
        }

        private boolean goToPoint(String point){
            try {
                if(!randomEvent()){
                    // 退出注册
                    phaser.arriveAndDeregister();
                    return false;
                }
                String name = Thread.currentThread().getName();
                System.out.println(name + " 花了 " + getRandomTime() + " 时间才到了" + point);
                phaser.arriveAndAwaitAdvance();
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }

        /**
         * 随机事件
         * 有人退出或加入
         */
        private boolean randomEvent(){
            int r = random.nextInt(100);
            String name = Thread.currentThread().getName();
            if (r <= 20){
                int tmpCount = 1;
                System.out.println(name + "：遇到了"+tmpCount+"个朋友，临时决定要加入旅游...");
                // 注册
                phaser.bulkRegister(tmpCount);
                for (int i = 0; i < tmpCount; i++) {
                    String freindName = name+"的"+friendCount.getAndIncrement()+"号朋友";
                    new Thread(new Tourism2(phaser), freindName).start();
                }
            }else if (r >= 80){
                System.out.println(name + "：突然有事要离开一下，临时决定要退出旅游....");
                return false;
            }
            return true;
        }

        private void tourism() {
            switch (phaser.getPhase()){
                case 0:
                    if(!goToStartingPoint()){
                        break;
                    }
                case 1:
                    if(!goToHotel()){
                        break;
                    }
                case 2:
                    if(!goToTourismPoint1()){
                        break;
                    }
                case 3:
                    if(!goToTourismPoint2()){
                        break;
                    }
                case 4:
                    if(!goToTourismPoint3()){
                        break;
                    }
                case 5:
                    if(!goToEndPoint()){
                        break;
                    }
                default:
                    break;
            }
        }

        @Override
        public void run() {
            tourism();
        }
    }

     static class SubPhaser extends Phaser{
        public SubPhaser(int parties) {
            super(parties);
        }

        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            System.out.println(Thread.currentThread().getName() + "：全部"+getArrivedParties()+"个人都到齐了，现在是第"+(phase + 1)
                    +"次集合，准备去"+POINT_MAP.get(phase+1)+".........\n");
            return super.onAdvance(phase, registeredParties);
        }
    }
}
