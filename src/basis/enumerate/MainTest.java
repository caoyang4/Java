package src.basis.enumerate;

/**
 * @author caoyang
 */
public class MainTest {
    public static void main(String[] args) {
        for (FruitEnum fruitEnum: FruitEnum.values()){
            System.out.println("index: "+fruitEnum.getIndex() + " name: " + fruitEnum.getFruitName());
        }

        switchFruit(FruitEnum.PEAR);
        switchFruit(FruitEnum.LEMON);

        for (ResponseCodeEnum responseCodeEnum: ResponseCodeEnum.values()){
            System.out.println(responseCodeEnum.getDescription());
        }

    }

    public static void switchFruit(FruitEnum type){
        switch (type){
            case PEAR:
                System.out.println("悟空喜欢吃" + type.getFruitName());
                break;
            case PEACH:
                System.out.println("八戒不喜欢吃" + type.getFruitName());
                break;
            case GRAPE:
                System.out.println("哪吒喜欢吃" + type.getFruitName());
                break;
            case LEMON:
                System.out.println("铁扇公主喜欢吃" + type.getFruitName());
                break;
            case WATERMELON:
                System.out.println("路飞喜欢吃" + type.getFruitName());
                break;
            case ORANGE:
                System.out.println("佐助喜欢吃" + type.getFruitName());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
