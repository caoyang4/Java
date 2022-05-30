package src.jvm;

/**
 * 对象初始化
 * @author caoyang
 * @create 2022-05-30 21:34
 */
public class ObjectInitTest {
    static class Father{
        public Father() {
            System.out.println("Father init");
            // 即使在父类构造器，也不一定调用自己的 eat() 方法，
            // 多态机制，可能调用子类的 eat() 方法，
            eat();
        }
        public void eat(){
            System.out.println("father eat beef");
        }
    }
    static class Child extends Father{
        public Child() {
            System.out.println("Child init");
        }

        @Override
        public void eat() {
            System.out.println("child eat sugar");
        }
    }

    public static void main(String[] args) {
        // 子类创建实例，也会调用父类的构造器
        new Child();
    }
}
