package src.designPattern.composite;

/**
 * @author caoyang
 * @create 2022-05-26 18:29
 */
public class CompositePattern {
    public static void main(String[] args) {
        Component root = new Branch();
        Component branch1 = new Branch();
        Component branch2 = new Branch();
        root.add(branch1);
        root.add(branch2);
        branch1.add(new Leaf("b1-leaf1"));
        branch1.add(new Leaf("b1-leaf2"));
        branch2.add(new Leaf("b2-leaf1"));
        branch2.add(new Leaf("b2-leaf2"));
        root.operation();
    }
}
