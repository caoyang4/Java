package src.designPattern.iterator;

/**
 * @author caoyang
 */
public interface MyCollection {
    /**
     * 添加元素方法
     * @param o
     */
    void add(Object o);

    /**
     * 容量
     * @return
     */
    int size();

    /**
     * 迭代器
     * @return
     */
    MyIterator iterator();
}
