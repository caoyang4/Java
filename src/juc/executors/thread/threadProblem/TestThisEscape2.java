package src.juc.executors.thread.threadProblem;


public class TestThisEscape2 {
    private final int a;
    private TestThisEscape2 thisObj;

    public TestThisEscape2() {
        // 变量 a 和 thisObj 没有依赖，可能发生重排序，多线程场景下，导致 this 溢出
        a = 1;
        thisObj = this;
    }

    public void writer() {
        new TestThisEscape2();
    }

    public void reader() {
        if (thisObj != null) {
            // thisObj.a 可能为 0
            int temp = thisObj.a;
        }
    }
}
