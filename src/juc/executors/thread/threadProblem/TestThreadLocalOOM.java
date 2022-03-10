package src.juc.executors.thread.threadProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 设置堆最大为 64M，Xmx64m
 * @author caoyang
 */
public class TestThreadLocalOOM {
    private static final int THREAD_LOOP_SIZE = 500;
    private static final int MOCK_BIG_DATA_LOOP_SIZE = 10000;

    private static ThreadLocal<List<User>> threadLocal = new ThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_LOOP_SIZE);

        for (int i = 0; i < THREAD_LOOP_SIZE; i++) {
            executorService.execute(() -> {
                threadLocal.set(new TestThreadLocalOOM().addBigList());
                Thread t = Thread.currentThread();
                System.out.println(Thread.currentThread().getName() + ": " + threadLocal.get().size());
                // 执行remove()，可以避免threadLocalOOM
                // threadLocal.remove();
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private List<User> addBigList() {
        List<User> params = new ArrayList<>(MOCK_BIG_DATA_LOOP_SIZE);
        for (int i = 0; i < MOCK_BIG_DATA_LOOP_SIZE; i++) {
            try {
                params.add(new User("james", "password" + i, "male", i));
            } catch (OutOfMemoryError e){
                System.out.println("堆内存耗尽，GC发生内存泄漏...");
                e.printStackTrace();
                System.exit(0);
            }
        }
        return params;
    }

    static class User {
        private String userName;
        private String password;
        private String sex;
        private int age;

        public User(String userName, String password, String sex, int age) {
            this.userName = userName;
            this.password = password;
            this.sex = sex;
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                    "userName='" + userName + '\'' +
                    ", password='" + password + '\'' +
                    ", sex='" + sex + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
