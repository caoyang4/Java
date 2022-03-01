package src.basis.fish;

import org.openjdk.jol.info.ClassLayout;

/**
 * Object obj = new Object() 占了多少字节
 * @author caoyang
 */
public class TestObject {
    public static void main(String[] args) {
        // obj的对象头占8字节，class指针占4字节，由于要求是8的倍数，补齐4字节，总共 8+4+[4]=16 字节
        Object obj = new Object();
        System.out.println(ClassLayout.parseInstance(obj).toPrintable());
        System.out.println();

        // subObj的对象头占8字节，class指针占4字节，String成员变量占4字节，long成员变量占8字节,无需补齐，总共 8+4+4+8=24 字节
        ChildTestObject childObj = new ChildTestObject();
        System.out.println(ClassLayout.parseInstance(childObj).toPrintable());
        System.out.println();

        // 继承ChildTestObject，包括private成员变量，加上本类的int成员变量，补齐4字节，总共占 24+4+[4]=32 字节
        GrandTestObject grandObj = new GrandTestObject();
        System.out.println(ClassLayout.parseInstance(grandObj).toPrintable());

    }
}
class ChildTestObject{
    public String s;
    private long i;
}
class GrandTestObject extends ChildTestObject{
    public int j;
}