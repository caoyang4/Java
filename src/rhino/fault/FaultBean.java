
package src.rhino.fault;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author wanghao on 17/8/21.
 */
public class FaultBean {
    private long id;
    private FaultBeanAction action;
    private int type;
    private float sampleRate;
    private boolean isRandomDelay;
    private int maxDelay;
    private String exceptionType;
    private String mockValue;
    private Date startTime;
    private Date endTime;
    private boolean dyeEnabled;
    private int arch;
    private Set<String> ips;
    private Map<String, String> singleValueFeatures;
    private Map<String,Set<String>> multiValueFeatures;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public FaultBeanAction getAction() {
        return action;
    }

    public void setAction(FaultBeanAction action) {
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public boolean getRandomDelay() {
        return isRandomDelay;
    }

    public void setRandomDelay(boolean randomDelay) {
        isRandomDelay = randomDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean getDyeEnabled() {
        return dyeEnabled;
    }

    public void setDyeEnabled(boolean dyeEnabled) {
        this.dyeEnabled = dyeEnabled;
    }

    public Map<String, String> getSingleValueFeatures() {
        return singleValueFeatures;
    }

    public void setSingleValueFeatures(Map<String, String> singleValueFeatures) {
        this.singleValueFeatures = singleValueFeatures;
    }

    public Map<String, Set<String>> getMultiValueFeatures() {
        return multiValueFeatures;
    }

    public void setMultiValueFeatures(Map<String, Set<String>> multiValueFeatures) {
        this.multiValueFeatures = multiValueFeatures;
    }

    public Set<String> getIps() {
        return ips;
    }

    public void setIps(Set<String> ips) {
        this.ips = ips;
    }


    public int getArch() {
        return arch;
    }

    public void setArch(int arch) {
        this.arch = arch;
    }

    @Override
    public String toString() {
        return "FaultBean{" + "type=" + type + ", sampleRate=" + sampleRate + ", isRandomDelay=" + isRandomDelay + ", maxDelay=" + maxDelay + ", exceptionType='" + exceptionType + '\'' + ", mockValue='" + mockValue + '\'' + ", startTime=" + startTime + ", endTime=" + endTime + ", dyeEnabled=" + dyeEnabled + ", arch=" + arch + ", ips=" + ips + ", singleValueFeatures=" + singleValueFeatures + ", multiValueFeatures=" + multiValueFeatures + '}';
    }

    public String getMockValue() {
        return mockValue;
    }

    public void setMockValue(String mockValue) {
        this.mockValue = mockValue;
    }
}
