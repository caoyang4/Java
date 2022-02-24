package src.basis.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author caoyang
 */
public class TestRelection {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        Class clz = Dog.class;
        System.out.println("类名信息：");
        System.out.println(clz.getName());
        System.out.println(clz.getSimpleName());
        System.out.println(clz.getCanonicalName());
        System.out.println();
        System.out.println("父类信息");
        System.out.println(clz.getSuperclass());
        System.out.println();
        System.out.println("接口信息");
        for (Class anInterface : clz.getInterfaces()) {
            System.out.println(anInterface);
        }
        System.out.println();
        System.out.println("创建对象");
        Dog dog = (Dog) clz.newInstance();
        System.out.println(dog);
        System.out.println();
        System.out.println("字段信息");
        for (Field field : clz.getFields()) {
            System.out.println(field);
        }
        System.out.println();
        for (Field declaredField : clz.getDeclaredFields()) {
            System.out.println(declaredField);
        }
        System.out.println();
        System.out.println("方法信息");
        // 所有类，父类，接口的 public 方法
        for (Method method : clz.getMethods()) {
            System.out.println(method);
        }
        System.out.println();
        // 所有本类的方法
        for (Field declaredField : clz.getDeclaredFields()) {
            System.out.println(declaredField);
        }
        System.out.println();

        System.out.println("构造器信息");
        for (Constructor constructor : clz.getConstructors()) {
            System.out.println(constructor);
        }
        System.out.println();
        for (Constructor declaredConstructor : clz.getDeclaredConstructors()) {
            System.out.println(declaredConstructor);
        }
        System.out.println();

        System.out.println("公有构造器创建对象");
        Constructor constructor1 = clz.getDeclaredConstructor(String.class);
        Dog dogx = (Dog) constructor1.newInstance("huahua");
        System.out.println(dogx);

        System.out.println("私有构造器创建对象");
        Constructor constructor2 = clz.getDeclaredConstructor(String.class, Integer.class);
        constructor2.setAccessible(true);
        Dog dogy = (Dog) constructor2.newInstance("huahua", 10);
        Field name = clz.getDeclaredField("name");
        name.setAccessible(true);
        System.out.println(dogy);
        name.set(dogy, "gouzi");
        System.out.println(dogy);
    }
}


interface I1 {
    void i1();
}
interface I2 {
    void i2();
}
class Cell{
    public int mCellPublic;
}
class Animal extends  Cell{
    private int mAnimalPrivate;
    protected int mAnimalProtected;
    int mAnimalDefault;
    public int mAnimalPublic;
    private static int sAnimalPrivate;
    protected static int sAnimalProtected;
    static int sAnimalDefault;
    public static int sAnimalPublic;
}
class Dog extends Animal implements I1, I2 {
    public Dog() {
    }

    public Dog(String name) {
        this.name = name;
    }

    private Dog(String name, Integer weight) {
        this.name = name;
        this.weight = weight;
    }

    private String name;
    private Integer weight;
    private int mDogPrivate;
    public int mDogPublic;
    protected int mDogProtected;
    private int mDogDefault;
    private static int sDogPrivate;
    protected static int sDogProtected;
    static int sDogDefault;
    public static int sDogPublic;

    @Override
    public void i1() {}

    @Override
    public void i2() {}

    @Override
    public String toString() {
        return "Dog{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                '}';
    }
}