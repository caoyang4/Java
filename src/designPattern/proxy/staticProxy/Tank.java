package src.designPattern.proxy.staticProxy;

/**
 * 静态代理
 * @author caoyang
 */
public class Tank implements Movable {
    @Override
    public void move() {
        System.out.println("...tank move...");
    }

    public static void main(String[] args) {
        // 静态代理嵌套
        Movable m = new TimeTank(
              new LogTank(
                      new Tank()
              )
        );
        m.move();
    }
}
