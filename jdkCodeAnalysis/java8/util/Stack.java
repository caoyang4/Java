package java.util;

/**
 * 栈，继承Vector，线程安全，不推荐使用
 * @param <E>
 */
public class Stack<E> extends Vector<E> {
    public Stack() {
    }
    // 入队
    public E push(E item) {
        addElement(item);
        return item;
    }
    // 弹出数组最后一个元素
    public synchronized E pop() {
        E       obj;
        int     len = size();

        obj = peek();
        removeElementAt(len - 1);

        return obj;
    }
    // 返回数组最后一个元素
    public synchronized E peek() {
        int     len = size();

        if (len == 0)
            throw new EmptyStackException();
        return elementAt(len - 1);
    }

    public boolean empty() {
        return size() == 0;
    }

    public synchronized int search(Object o) {
        int i = lastIndexOf(o);

        if (i >= 0) {
            return size() - i;
        }
        return -1;
    }

    private static final long serialVersionUID = 1224463164541339165L;
}
