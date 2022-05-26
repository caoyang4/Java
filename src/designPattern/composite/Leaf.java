package src.designPattern.composite;

/**
 * @author caoyang
 * @create 2022-05-26 18:26
 */
public class Leaf implements Component{
    private String name;
    public Leaf(String name) {
        this.name = name;
    }
    @Override
    public void add(Component c) {

    }

    @Override
    public void remove(Component c) {

    }

    @Override
    public void operation() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Leaf{" +
                "name='" + name + '\'' +
                '}';
    }
}
