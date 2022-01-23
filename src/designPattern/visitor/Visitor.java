package src.designPattern.visitor;

/**
 * @author caoyang
 */
public interface Visitor {
    void visitCpu(Component cpu);
    void visitMemory(Component memory);
    void visitBoard(Component board);
}

class PersonalBuyer implements Visitor{
    private float totalPrice;

    public float getTotalPrice() {
        return totalPrice;
    }

    @Override
    public void visitCpu(Component cpu) {
        totalPrice += cpu.getPrice() * 0.8;
    }

    @Override
    public void visitMemory(Component memory) {
        totalPrice += memory.getPrice() * 0.8;
    }

    @Override
    public void visitBoard(Component board) {
        totalPrice += board.getPrice() * 0.8;
    }
}

class CorpBuyer implements Visitor{
    private float totalPrice;

    public float getTotalPrice() {
        return totalPrice;
    }

    @Override
    public void visitCpu(Component cpu) {
        totalPrice += cpu.getPrice() * 0.6;
    }

    @Override
    public void visitMemory(Component memory) {
        totalPrice += memory.getPrice() * 0.6;
    }

    @Override
    public void visitBoard(Component board) {
        totalPrice += board.getPrice() * 0.6;
    }
}
