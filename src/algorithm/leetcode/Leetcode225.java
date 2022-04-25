package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;

/**
 * 225. 用队列实现栈
 * @author caoyang
 */
public class Leetcode225 {
    class MyStack {
        private List<Integer> list;
        public MyStack() {
            list = new ArrayList<>();
        }

        public void push(int x) {
            list.add(x);
        }

        public int pop() {
            int val = list.get(list.size()-1);
            list.remove(list.size()-1);
            return val;
        }

        public int top() {
            return list.get(list.size()-1);
        }

        public boolean empty() {
            return list.isEmpty();
        }
    }
}
