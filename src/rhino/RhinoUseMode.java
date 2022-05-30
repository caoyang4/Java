package src.rhino;

/**
 * @author zhanjun
 * @date 2017/11/22
 */
public enum RhinoUseMode {

    ANNOTATION(1, true),
    API(2, true),
    HYSTRIX(3, true),
    RPC_CLIENT(4, false);

    private int value;
    //该模式是否需要上报rhinoKey
    private boolean report;

    RhinoUseMode(int value, boolean report) {
        this.value = value;
        this.report = report;
    }

    public int getValue() {
        return value;
    }

    public boolean isReport() {
        return report;
    }
}
