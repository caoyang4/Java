package src.algorithm.leetcode;

import java.util.Deque;
import java.util.LinkedList;

/**
 * 622. 设计循环队列
 * 基于FIFO（先进先出）
 * @author caoyang
 */
public class Leetcode622 {
    class MyCircularQueue {

        Deque<Integer> list;
        int size = 0;
        int capacity;
        public MyCircularQueue(int k) {
            capacity = k;
            list = new LinkedList<>();
        }
        // 入队
        public boolean enQueue(int value) {
            if (list.size() >= capacity){
                return false;
            }
            if (size == list.size() || size < capacity){
                list.add(value);
                size++;
            } else {
                list.addFirst(value);
            }
            return true;
        }
        // 出队
        public boolean deQueue() {
            if(isEmpty()) return false;
            list.pop();
            size--;
            return true;
        }
        // 队首获取元素
        public int Front() {
            if (isEmpty()) return -1;
            return list.getFirst();
        }
        // 队尾获取元素
        public int Rear() {
            if (isEmpty()) return -1;
            return list.getLast();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public boolean isFull() {
            return list.size() == capacity;
        }
    }

    public static void main(String[] args) {

    }
}
