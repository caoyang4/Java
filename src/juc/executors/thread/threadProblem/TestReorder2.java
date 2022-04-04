package src.juc.executors.thread.threadProblem;

/**
 * 指令重排序验证2
 * @author caoyang
 */
public class TestReorder2 {
    public int sum = 0;
    public boolean flag = false;
    public int action1(){
        if (flag){
            return sum + sum;
        } else {
            return 1;
        }
    }
    public void action2(){
        sum = 1;
        flag = true;
    }

    public static void main(String[] args) {
        TestReorder2 test = new TestReorder2();
        int index = 0;
        while (true){
            index++;
            System.out.println("第"+index+"次" );
            new Thread(() -> {
                if(test.action1() == 0){
                    System.out.println("发生指令重排序");
                    System.exit(0);
                }
            }).start();
            new Thread(() -> {
                test.action2();
            }).start();
        }
    }

}
