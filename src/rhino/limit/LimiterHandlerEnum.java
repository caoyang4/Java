package src.rhino.limit;

/**
 * Created by zhanjun on 2017/6/7.
 */
public enum LimiterHandlerEnum {

    EXCEPTION(1),
    WAIT(2),
    DEGRADE(3);

    private int type;

    LimiterHandlerEnum(int type) {
        this.type = type;
    }

    public boolean isWait() {
        return this == WAIT;
    }

    public boolean isDegrade() {
        return this == DEGRADE;
    }

    public static LimiterHandlerEnum getStrategy(int type) {
        switch (type) {
            case 1: return EXCEPTION;
            case 2: return WAIT;
            case 3: return DEGRADE;
            default: return null;
        }
    }

    public int getType() {
        return type;
    }
}
