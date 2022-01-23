package src.designPattern.templateMethod;

/**
 * 模板方法模式 即钩子函数
 * @author caoyang
 */
public class TemplateMethod {
    public static void main(String[] args) {
        Animal animal = new Cat();
        animal.play();
    }
}

abstract class Animal{
    public void play(){
        move();
        fight();
    }

    abstract void fight();

    abstract void move();
}

class Cat extends Animal{
    @Override
    void fight() {
        System.out.println("无敌喵喵拳");
    }

    @Override
    void move() {
        System.out.println("猛虎下山");
    }
}
