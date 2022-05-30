package src.jvm;

/**
 * @author caoyang
 * @create 2022-05-30 21:34
 */
public class InitTest {
    static class Father{
        public Father() {
            System.out.println("Father init");
        }
    }
    static class Child extends Father{
        public Child() {
            System.out.println("Child init");
        }
    }

    public static void main(String[] args) {
        // 子类创建实例，也会调用父类的构造器
        new Child();
    }
}
