package src.designPattern.adapter;

/**
 * @author caoyang
 * @create 2022-05-25 15:22
 */
public class Adapter implements Transfer{
    Electricity electricity;

    public Adapter(Electricity electricity) {
        this.electricity = electricity;
    }

    @Override
    public int transfer() {
        int num = electricity.getElectricity();
        System.out.println("before adapter: "+num + "V");
        num /= 44;
        System.out.println("after adapter: "+num + "V");
        return num;
    }
}
