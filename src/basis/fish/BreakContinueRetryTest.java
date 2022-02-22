package src.basis.fish;

/**
 * break retry 跳到retry处，且不再进入循环
 * continue retry 跳到retry处，且再次进入循环
 * @author caoyang
 */
public class BreakContinueRetryTest {
    public static void main(String[] args) {
        System.out.println("breakRetry test");
        breakRetry();
        System.out.println();
        System.out.println("continueRetry test");
        continueRetry();
    }
    private static void breakRetry() {
        int i = 0;
        retry:
        for (; ; ) {
            for (; ; ) {
                i++;
                System.out.println(i);
                if (i == 4) { break retry;}
            }
        }
        //start 进入外层循环
        //4
        System.out.println("breakRetry finish: "+i);
    }
    private static void continueRetry() {
        int i = 0;
        retry:
        for(;;) {
            for(;;) {
                i++;
                if (i == 3) {continue retry;}
                System.out.println(i);
                if (i == 4) {break retry;}
            }
        }
        //start 第一次进入外层循环
        //end i=1输出
        //end i=2输出
        //start 再次进入外层循环
        //end i=4输出
        //4 最后输出
        System.out.println("continueRetry finish: " + i);
    }

}
