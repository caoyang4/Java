package src.basis.fish;

/**
 * 对象拷贝时，类的构造函数是不会被执行的
 * @author caoyang
 * @create 2022-05-28 13:53
 */
public class CloneTest implements Cloneable{
    public CloneTest() {
        System.out.println("CloneTest Constructor");
    }

    @Override
    protected CloneTest clone() throws CloneNotSupportedException {
        return (CloneTest) super.clone();
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        CloneTest test = new CloneTest();
        System.out.println("test: " + test);
        // clone()不会调用构造器
        CloneTest testClone = test.clone();
        System.out.println("testClone: " + testClone);
    }
}