package src.designPattern.factory.abstractFactory;

/**
 * @author caoyang
 */
public class Main {
    public static void main(String[] args) {
        // 成人世界产品族
        System.out.println("大人世界产品族");
        AbstractFatory adult = new AdultWorldFactory();
        Animal lion = adult.createAnimal();
        Toy robot = adult.createToy();
        lion.printName();
        robot.printName();

        System.out.println("=================");

        // 儿童世界产品族
        System.out.println("儿童世界产品族");
        AbstractFatory child = new ChildWordFactory();
        Animal cat = child.createAnimal();
        Toy doll = child.createToy();
        cat.printName();
        doll.printName();

    }
}
