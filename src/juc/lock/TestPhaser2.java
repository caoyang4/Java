package src.juc.lock;

import java.util.Random;
import java.util.concurrent.Phaser;

import static java.lang.System.out;

/**
 * @author caoyang
 */
public class TestPhaser2 {
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
    }
    public static void main(String[] args) {
        Phaser phaser = new TourismPhaser(persons.length);
        for (String person : persons) {
            new Thread(new Tourism1(phaser), person).start();
        }
    }

    static class Tourism1 implements Runnable{
        Phaser phaser;
        Random random;

        public Tourism1(Phaser phaser) {
            this.phaser = phaser;
            this.random = new Random();
        }

        @Override
        public void run() {
            goToStartingPoint();
            goToHotel();
            goToTourismPoint1();
            goToTourismPoint2();
            goToTourismPoint3();
            goToEndPoint();
        }
        /**
         * 返程回家
         */
        private void goToEndPoint() {
            goToPoint("飞机场，准备登机回家");
        }

        /**
         * 到达旅游点3
         */
        private void goToTourismPoint3() {
            goToPoint("旅游点3");
        }

        /**
         * 到达旅游点2
         */
        private void goToTourismPoint2() {
            goToPoint("旅游点2");
        }

        /**
         * 到达旅游点1
         */
        private void goToTourismPoint1() {
            goToPoint("旅游点1");
        }

        /**
         * 入住酒店
         */
        private void goToHotel() {
            goToPoint("酒店");
        }

        /**
         * 出发点集合
         */
        private void goToStartingPoint() {
            goToPoint("出发点");
        }

        private int getRandomTime(){
            int time = this.random.nextInt(400) + 100;
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return time;
        }

        private void goToPoint(String point){
            try {
                String name = Thread.currentThread().getName();
                out.println(name + " 花了 " + getRandomTime() + " 时间才到了" + point);
                phaser.arriveAndAwaitAdvance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    static class TourismPhaser extends Phaser{
        public TourismPhaser(int length) {
            super(length);
        }

        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            switch (phase) {
                case 0:
                    out.println("所有人到达出发点，准备出发！");
                    out.println("*********************************");
                    return false;
                case 1:
                    out.println("所有人到达酒店，准备入住！");
                    out.println("*********************************");
                    return false;
                case 2:
                    out.println("所有人到达景点1，开始游玩！");
                    out.println("*********************************");
                    return false;
                case 3:
                    out.println("所有人到达景点2，开始游玩！");
                    out.println("*********************************");
                    return false;
                case 4:
                    out.println("所有人到达景点3，开始游玩！");
                    out.println("*********************************");
                    return false;
                case 5:
                    out.println("所有人到达机场，准备回家！");
                    out.println("*********************************");
                    return true;
                default:
                    return true;
            }
        }
    }
}
