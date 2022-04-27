package src.algorithm.leetcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 232. 用栈实现队列
 * @author caoyang
 */
public class Leetcode232 {
    class MyQueue {
        Stack<Integer> in;
        Stack<Integer> out;
        public MyQueue() {
            in = new Stack<>();
            out = new Stack<>();
        }

        public void push(int x) {
            in.push(x);
        }

        public int pop() {
            if(out.empty()){in2out();}
            return out.pop();
        }

        public int peek() {
            if(out.empty()){in2out();}
            return out.peek();
        }

        public boolean empty() {
            return in.isEmpty() && out.empty();
        }

        private void in2out(){
            while (!in.isEmpty()){
                out.push(in.pop());
            }
        }
    }

    public static void main(String[] args) {

    }

}
