package src.juc.lock;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Phaser;

public class TestPhaser3 {
    public static void main(String[] args) {
        String visitor = "明刚红丽黑白";
        String kongjie = "美惠花";

        Airplane airplane = new Airplane(visitor.length());
        Set<Thread> threads = new HashSet<>();
        for (int i = 0; i < visitor.length(); i ++){
            threads.add(new Thread(() -> {
                airplane.getOffPlane();
            }, "小" + visitor.charAt(i)));
        }
        for (int i = 0; i < kongjie.length(); i ++){
            threads.add(new Thread(() ->{
                airplane.doWork();
            }, "小" + kongjie.charAt(i) + "空姐"));
        }

        for (Thread thread : threads){
            thread.start();
        }
    }
    static class Airplane{
        private Phaser phaser;
        private Random random;
        public Airplane(int peopleNum){
            phaser = new Phaser(peopleNum);
            random = new Random();
        }

        /**
         * 下机
         */
        public void getOffPlane(){
            try {
                String name = Thread.currentThread().getName();
                Thread.sleep(random.nextInt(500));
                System.out.println(name + " 在飞机在休息着....");
                Thread.sleep(random.nextInt(500));
                System.out.println(name + " 下飞机了");
                phaser.arrive();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void doWork(){

            String name = Thread.currentThread().getName();
            System.out.println(name + "准备做【清理】工作");
            phaser.awaitAdvance(phaser.getPhase());
            System.out.println("飞机的乘客都下机," + name + "可以开始做【清理】工作");

        }
    }
}
