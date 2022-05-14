package src.container;

import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.Set;

/**
 * 枚举集合
 */
@Slf4j(topic = "TestEnumSet")
public class TestEnumSet {
    enum Color{
        RED(0,"红色"),
        BLUE(1,"蓝色"),
        GREEN(2,"绿色"),
        BLACK(3, "黑色");

        int id;
        String name;
        Color(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    public static void main(String[] args) {
        Set<Color> colors = EnumSet.noneOf(Color.class);
        log.info("noneof {}", colors);
        colors = EnumSet.allOf(Color.class);
        log.info("allOf {}", colors);
        colors = EnumSet.of(Color.RED, Color.BLACK);
        log.info("of {}", colors);
        colors = EnumSet.range(Color.RED, Color.GREEN);
        log.info("range {}", colors);
    }

}
