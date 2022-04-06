package src.juc.pratice;


import lombok.Data;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author caoyang
 */
public class TestUnsafe {
    public static void main(String[] args) throws Exception {
        Constructor constructor = Unsafe.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Unsafe unsafe = (Unsafe) constructor.newInstance();
        System.out.println(unsafe);

        // 获取域
        Student student = new Student();
        System.out.println(student);
        long idOffset = unsafe.objectFieldOffset(Student.class.getDeclaredField("id"));
        long nameOffset = unsafe.objectFieldOffset(Student.class.getDeclaredField("name"));
        unsafe.compareAndSwapInt(student, idOffset, 0, 18);
        unsafe.compareAndSwapObject(student, nameOffset, null, "james");
        System.out.println(student);
    }

    @Data
    static class Student{
        int id;
        String name;

        @Override
        public String toString() {
            return "Student{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
