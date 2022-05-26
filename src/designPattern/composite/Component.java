package src.designPattern.composite;

/**
 * 组合模式
 * @author caoyang
 * @create 2022-05-26 18:22
 */
public interface Component {
    void add(Component c);
    void remove(Component c);
    void operation();
}
