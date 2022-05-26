package src.designPattern.composite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author caoyang
 * @create 2022-05-26 18:27
 */
public class Branch implements Component{
    private List<Component> children = new ArrayList<>();
    @Override
    public void add(Component c) {
        children.add(c);
    }

    @Override
    public void remove(Component c) {
        children.remove(c);
    }

    @Override
    public void operation() {
        children.forEach(Component::operation);
    }
}
