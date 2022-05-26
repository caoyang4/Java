package src.designPattern.bridge;

/**
 * @author caoyang
 * @create 2022-05-26 12:25
 */
public class FoodCompany extends Company{
    private Product product;

    public FoodCompany(Product product) {
        super(product);
        this.product = product;
    }

    @Override
    public void makeMoney() {
        super.makeMoney();
        System.out.println("food company make money");
    }
}
