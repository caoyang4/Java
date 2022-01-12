package src.designPattern.factory.abstractFactory;

/**
 * 成人世界工厂
 * @author caoyang
 */
public class AdultWorldFactory extends AbstractFatory{
    @Override
    Animal createAnimal() {
        return new Lion();
    }

    @Override
    Toy createToy() {
        return new Robot();
    }
}
