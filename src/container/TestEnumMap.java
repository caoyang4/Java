package src.container;

import java.util.EnumMap;
import java.util.Map;

/**
 * 枚举 map，其底层为数组实现，数组长度为枚举值的个数
 */
public class TestEnumMap {
    enum Animal{
        CAT(0,"maomi"),
        DOG(1,"gouzi"),
        HORSE(2,"maer");
        int id;
        String name;

        Animal(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static void main(String[] args) {
        Map<Animal, String> map = new EnumMap<Animal, String>(Animal.class);
        map.put(Animal.CAT, "喵喵喵");
        map.put(Animal.DOG, "汪汪汪");
        map.put(Animal.HORSE, "嘶嘶嘶");
        for (Animal animal : map.keySet()) {
            System.out.println(animal);
        }
    }
}
