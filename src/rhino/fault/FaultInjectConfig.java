package src.rhino.fault;

public class FaultInjectConfig {

    private boolean isActive;

    private boolean isDelay;

    private int delayTime;

    public FaultInjectConfig(boolean isActive, boolean isDelay, int delayTime) {
        this.isActive = isActive;
        this.isDelay = isDelay;
        this.delayTime = delayTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isDelay() {
        return isDelay;
    }

    public int getDelayTime() {
        return delayTime;
    }
}
