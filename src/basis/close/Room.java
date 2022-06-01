package src.basis.close;

import sun.misc.Cleaner;

/**
 * @author caoyang
 * @create 2022-06-01 19:38
 */
public class Room implements AutoCloseable{
    private static Cleaner cleaner;
    private final State state;
    public Room() {
        state = new State(10);
        cleaner = Cleaner.create(this, state);
    }

    private static class State implements Runnable{
        int numJunk;
        State(int numJunk){
            this.numJunk = numJunk;
        }

        @Override
        public void run() {
            System.out.println("clean room");
            numJunk = 0;
        }
    }
    @Override
    public void close() throws Exception {
        System.out.println("bye-bye");
        // 调用 run() 方法，并不是起线程
        cleaner.clean();
    }

    public static void main(String[] args) throws Exception {
        // 退出 try-with-resources 块时，会调用 close() 方法
        try (Room room = new Room()){
            System.out.println("good job");
        }
    }
}
