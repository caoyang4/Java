package src.designPattern.factory.abstractFactory;

/**
 * 儿童世界工厂
 * @author caoyang
 */
public class ChildWordFactory extends AbstractFatory{

    @Override
    Animal createAnimal() {
        return new Cat();
    }

    @Override
    Toy createToy() {
        return new Doll();
    }
}
