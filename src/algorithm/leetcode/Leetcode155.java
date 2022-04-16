package src.algorithm.leetcode;

import java.util.Stack;

/**
 * 155. 最小栈
 * @author caoyang
 */
public class Leetcode155 {
    static class MinStack{
        private Stack<Integer> stack;
        private Stack<Integer> minStack;
        public MinStack() {
            stack = new Stack();
            minStack = new Stack();
        }
        public void push(int val) {
            stack.push(val);
            minStack.push(minStack.isEmpty() ? val : Math.min(val, minStack.peek()));
        }

        public void pop() {
            stack.pop();
            minStack.pop();
        }

        public int top() { return stack.peek(); }

        public int getMin() { return minStack.peek(); }
    }

    public static void main(String[] args) {
        MinStack stack = new MinStack();
        stack.push(-2);
        stack.push(0);
        stack.push(-3);
        System.out.println(stack.getMin());
        stack.pop();
        System.out.println(stack.top());

    }
}
