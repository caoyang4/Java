package src.designPattern.bridge;

/**
 * @author caoyang
 * @create 2022-05-26 12:28
 */
public class User {
    public static void main(String[] args) {
        Company food = new FoodCompany(new Hamburg());
        food.makeMoney();
        Company house = new HouseCompany(new Department());
        house.makeMoney();
    }
}
