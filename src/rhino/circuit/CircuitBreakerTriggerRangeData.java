package src.rhino.circuit;

/**
 * @author zhanjun
 * @date 2017/11/13
 */
public class CircuitBreakerTriggerRangeData {

    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private float errorThresholdPercentage;
    private int errorThresholdCount;
    private int requestVolumeThreshold;

    public boolean isMatch(int hour, int minute) {
        return (hour > startHour || hour == startHour && minute >= startMinute) &&
                (hour < endHour || hour == endHour && minute <= endMinute);
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public float getErrorThresholdPercentage() {
        return errorThresholdPercentage;
    }

    public void setErrorThresholdPercentage(float errorThresholdPercentage) {
        this.errorThresholdPercentage = errorThresholdPercentage;
    }

    public int getErrorThresholdCount() {
        return errorThresholdCount;
    }

    public void setErrorThresholdCount(int errorThresholdCount) {
        this.errorThresholdCount = errorThresholdCount;
    }

    public int getRequestVolumeThreshold() {
        return requestVolumeThreshold;
    }

    public void setRequestVolumeThreshold(int requestVolumeThreshold) {
        this.requestVolumeThreshold = requestVolumeThreshold;
    }
}
