package src.designPattern.bridge;

/**
 * @author caoyang
 * @create 2022-05-26 12:27
 */
public class HouseCompany extends Company{
    Product product;
    public HouseCompany(Product product) {
        super(product);
        this.product = product;
    }

    @Override
    public void makeMoney() {
        super.makeMoney();
        System.out.println("house company make money");
    }
}
