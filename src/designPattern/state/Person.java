package src.designPattern.state;

import lombok.Data;

/**
 * 状态模式
 * 模式的行为（即方法）确定，状态可扩展
 * @author caoyang
 */
@Data
public class Person {
    private String name;
    private StateAction action;
    void behave(){
        action.laugh();
        action.shout();
        action.cry();
    }

    public static void main(String[] args) {
        Person person = new Person();
        person.setName("james");
        person.setAction(new HappyStateAction());
        person.behave();
    }
}

class HappyStateAction implements StateAction{
    @Override
    public void laugh() {
        System.out.println("hahaha....");
    }

    @Override
    public void shout() {
    }

    @Override
    public void cry() {
    }
}

class SadStateAction implements StateAction{
    @Override
    public void laugh() {

    }

    @Override
    public void shout() {

    }

    @Override
    public void cry() {
        System.out.println("wawawa...");
    }
}

class AngryStateAction implements StateAction{
    @Override
    public void laugh() {

    }

    @Override
    public void shout() {
        System.out.println("fufufu...");
    }

    @Override
    public void cry() {

    }
}

