package src.basis.enumerate;

/**
 * @author caoyang
 */

public enum FruitEnum {
    /**
     * 水果
     */
    PEAR(0, "梨子"),
    PEACH(1, "桃子"),
    GRAPE(2, "葡萄"),
    ORANGE(3, "橘子"),
    LEMON(4, "柠檬"),
    WATERMELON(5, "西瓜");

    private final int index;
    private final String fruitName;


    FruitEnum(int index, String fruitName) {
        this.index = index;
        this.fruitName = fruitName;
    }

    public int getIndex() {
        return index;
    }

    public String getFruitName() {
        return fruitName;
    }
}
