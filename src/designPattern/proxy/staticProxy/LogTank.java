package src.designPattern.proxy.staticProxy;

public class LogTank implements Movable{
    private Movable m;

    public LogTank(Movable m) {
        this.m = m;
    }

    @Override
    public void move() {
        System.out.println("[log proxy] tank comes...");
        m.move();
        System.out.println("[log proxy] tank leaves...");
    }
}
