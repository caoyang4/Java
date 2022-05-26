package src.designPattern.bridge;

/**
 * @author caoyang
 * @create 2022-05-26 12:22
 */
public abstract class Company {
    Product product;

    public Company(Product product) {
        this.product = product;
    }

    public void makeMoney(){
        product.produce();
        product.sell();
    }
}
