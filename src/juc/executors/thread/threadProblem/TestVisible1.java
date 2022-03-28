package src.juc.executors.thread.threadProblem;

/**
 * 可见性问题
 * @author caoyang
 */
public class TestVisible1 {
    public static void main(String[] args){
        A a = new A();

        new Thread(() -> {
            while (true){
                a.setI();
            }
        }).start();

        new Thread(() -> {
            while (true){
                System.out.println(a.i);
                if(a.i == 0){
                    System.out.println("it happens visible problem ...");
                    System.exit(0);
                }
            }
        }).start();
    }
}

class A{
    public int i;

    public void setI() {
        i = 0;
        int j = 10;
        i = j;
    }

}
