package src.rhino.limit.feature;

/**
 * Created by zhanjun on 2017/8/13.
 */
public enum MatchModeEnum {

    MATCH_ALL(1),
    MATCH_ANY(2);

    private int value;

    MatchModeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MatchModeEnum getByValue(int value) {
        switch (value) {
            case 1: return MATCH_ALL;
            case 2: return MATCH_ANY;
            default: return null;
        }
    }
}
