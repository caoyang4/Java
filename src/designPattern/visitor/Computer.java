package src.designPattern.visitor;

/**
 * 访问者模式适合结构或模型固定的对象
 * ASM
 * @author caoyang
 */
public class Computer {
    Component cpu = new Cpu();
    Component memory = new Memory();
    Component board = new Board();
    public void accept(Visitor visitor){
        cpu.accept(visitor);
        memory.accept(visitor);
        board.accept(visitor);
    }

    public static void main(String[] args) {
        PersonalBuyer personalBuyer = new PersonalBuyer();
        Computer computer = new Computer();
        computer.accept(personalBuyer);
        System.out.println("个人购买电脑价格：" + personalBuyer.getTotalPrice());

        CorpBuyer corpBuyer = new CorpBuyer();
        computer.accept(corpBuyer);
        System.out.println("企业购买电脑价格："+corpBuyer.getTotalPrice());

    }

}

abstract class Component{
    abstract void accept(Visitor visitor);

    abstract int getPrice();
}

class Cpu extends Component{
    private static final int CPU_PRICE = 400;
    @Override
    void accept(Visitor visitor) {
        visitor.visitCpu(this);
    }

    @Override
    int getPrice() {
        return CPU_PRICE;
    }
}

class Memory extends Component{
    private static final int MEMORY_PRICE = 600;
    @Override
    void accept(Visitor visitor) {
        visitor.visitMemory(this);
    }

    @Override
    int getPrice() {
        return MEMORY_PRICE;
    }
}

class Board extends Component{
    private static final int BOARD_PRICE = 1000;
    @Override
    void accept(Visitor visitor) {
        visitor.visitBoard(this);
    }

    @Override
    int getPrice() {
        return BOARD_PRICE;
    }
}
