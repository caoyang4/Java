package src.rhino.circuit;


/**
 * @author zhanjun
 */
public enum RequestStatus {

    NORMAL,
    DEGRADE,
    SINGLE_TEST;

    public boolean isDegrade() {
        return this == DEGRADE;
    }

    public boolean isSingleTest() {
        return this == SINGLE_TEST;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }
}