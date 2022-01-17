package src.designPattern.proxy.staticProxy;

import java.util.Random;

/**
 * @author caoyang
 */
public class TimeTank implements Movable{
    private Movable m;

    public TimeTank(Movable m) {
        this.m = m;
    }

    @Override
    public void move() {
        System.out.println("[time proxy] tank start move");
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(new Random().nextInt(10000));
            m.move();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("[time proxy] tank end move, total " + (end - start) + "ms");
    }
}
