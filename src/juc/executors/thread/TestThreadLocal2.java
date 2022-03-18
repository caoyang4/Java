package src.juc.executors.thread;

import java.util.Random;

/**
 * @author caoyang
 */
public class TestThreadLocal2 {
    private static ThreadLocal<Student> locals = new ThreadLocal<>();

    public static void main(String[] args) {
        new TestThreadLocal2Thread("t1").start();
        new TestThreadLocal2Thread("t2").start();
    }

    static class TestThreadLocal2Thread extends Thread{
        public TestThreadLocal2Thread(String name) {
            super(name);
        }

        @Override
        public void run() {
            String currentThreadName = getName();
            System.out.println(currentThreadName + " is running...");
            Random random = new Random();
            int age = random.nextInt(100);
            System.out.println(currentThreadName + "  set age: "  + age);
            Student student = getStudent();
            student.setAge(age);
            System.out.println(currentThreadName + " first get age: " + student.getAge());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println( currentThreadName + " second get age: " + student.getAge());
        }

        private Student getStudent(){
            Student student = locals.get();
            if (student == null){
                student = new Student();
                locals.set(student);
            }
            return student;
        }
    }

    static class Student{
        private int age;
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }
    }
}
