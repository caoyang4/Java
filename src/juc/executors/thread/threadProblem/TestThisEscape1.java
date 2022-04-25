package src.juc.executors.thread.threadProblem;

/**
 * this溢出
 * 未完成的实例化TestThisEscapeChild对象，却已经暴露了对象的引用
 * 其他线程访问还没有构造好的对象，可能会造成意料不到的问题
 * @author caoyang
 */
public class TestThisEscape1 {
    public TestThisEscape1() {
        System.out.println("construct ThisEscape");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 调用ThisEscape类中的方法，内部类具有对外部类this的应用
                say();
            }
        });
        // 此时开启线程，可能在调用Say()时会导致不可预见性的结果
        thread.start();
    }

    public void say() {
        System.out.println("ThisEscape.Say()");
    }

    public static void main(String[] args) {
        while (true){
            new TestThisEscapeChild();
        }
    }
}

class TestThisEscapeChild extends TestThisEscape1 {
    private final String name;

    public TestThisEscapeChild() {
        this.name = "Hello Word";
        System.out.println("construct ThisEscapeChild");
    }

    public TestThisEscapeChild(String name) {
        this.name = name;
        System.out.println("ThisEscapeChild");
    }

    @Override
    public void say() {
        if(name == null){
            System.out.println("it happens this escape!");
            System.exit(0);
        }
        System.out.println("ThisEscapeChild.say()-->" + name);
    }
}
