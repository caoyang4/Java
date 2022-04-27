package src.algorithm.leetcode;

import java.util.*;

/**
 * 225. 用队列实现栈
 * @author caoyang
 */
public class Leetcode225 {
    class MyStack {
        private Deque<Integer> in;
        private Deque<Integer> out;
        public MyStack() {
            in = new LinkedList<>();
            out = new LinkedList<>();
        }

        public void push(int x) {
            in.add(x);
        }

        public int pop() {
            in2out();
            return out.pop();
        }

        public int top() {
            in2out();
            return out.peek();
        }

        public boolean empty() {
            return in.isEmpty() && out.isEmpty();
        }
        private void in2out(){
            while (!in.isEmpty()){
                out.push(in.pop());
            }
        }
    }

    class MyStack1 {
        private List<Integer> list;
        public MyStack1() {
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

    public static void main(String[] args) {

    }
}
