package src.juc.executors.thread.threadProblem;

/**
 * 指令重排序验证
 * @author caoyang
 */
public class TestReorder {
    private static int x =0, y =0;
    private static int a =0, b =0;

    public static void main(String[] args) throws InterruptedException {
        int i =0;

        for (;;){
            i++;
            x =0; y =0;
            a =0; b =0;
            Thread t1 =new Thread(() -> {
                shortWait(20000);
                a =1;
                x =b;
            });

            Thread t2 =new Thread(() -> {
                b =1;
                y =a;
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            String result ="第" + i +"次, (x,y): (" +x +"," +y +"）";
            if(x == 0 && y == 0) {
                System.out.println(result + "，发生指令重排序！");
                break;
            }else {
                System.out.println(result);
            }
        }
    }
    public static void shortWait(long interval){
        long start = System.nanoTime();
        long end;
        do{
            end = System.nanoTime();
        }while(start + interval >= end);
    }

}

