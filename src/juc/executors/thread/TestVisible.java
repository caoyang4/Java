package src.juc.executors.thread;

public class TestVisible {
    public static void main(String[] args){
        A a = new A();

        new Thread(() -> {
            while (true){
                a.setI();
            }
        }).start();

        new Thread(() -> {
            while (true){
                System.out.println(a.getI());
                if(a.getI() == 0){
                    System.out.println("it happens visible problem ...");
                    System.exit(0);
                }
            }
        }).start();
    }
}

class A{
    private int i;

    public void setI() {
        i = 0;
        int j = 10;
        i = j;
    }

    public int getI() {
        return i;
    }
}
