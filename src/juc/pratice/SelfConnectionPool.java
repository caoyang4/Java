package src.juc.pratice;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * 自定义连接池实现
 * @author caoyang
 */
public class SelfConnectionPool {
    private final int capacity;
    Connection[] connections;
    AtomicIntegerArray states;

    public SelfConnectionPool(int capacity) {
        this.capacity = capacity;
        connections = new Connection[capacity];
        states = new AtomicIntegerArray(capacity);
    }

    /**
     * 获取连接
     */
    public Connection getConnection(){
        while (true) {
            for (int i = 0; i < capacity; i++) {
                // cas置连接状态变为已占用
                if(states.get(i) == 0 && states.compareAndSet(i, 0, 1)){
                    return connections[i];
                }
            }
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 归还连接
     */
    public void returnConnection(Connection conn){
        boolean flag = false;
        for (int i = 0; i < capacity; i++) {
            if(connections[i] == conn){
                states.set(i, 0);
                flag = true;
                break;
            }
        }
        if (flag) {
            synchronized (this){
                notifyAll();
            }
        }
    }
}
