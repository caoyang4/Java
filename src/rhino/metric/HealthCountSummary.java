package src.rhino.metric;

/**
 * Created by zhanjun on 2017/6/11.
 */
public class HealthCountSummary {

    private long totalCount;
    private long successCount;
    private long errorCount;
    private float errorPercentage;
    private int qps;

    /**
     *
     * @param totalCount
     * @param errorCount
     */
    public HealthCountSummary(long totalCount, long errorCount) {
        this.totalCount = totalCount;
        this.errorCount = errorCount;
        this.errorPercentage = 1.0f * errorCount / totalCount * 100;
    }

    /**
     *
     * @param successCount
     * @param errorCount
     * @param qps
     */
    public HealthCountSummary(long successCount, long errorCount, int qps) {
        this.totalCount = successCount + errorCount;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.qps = qps;
    }

    public HealthCountSummary plus(HealthCount healthCount) {
        long successCount = this.successCount + healthCount.getSuccessCount();
        long failedCount = this.errorCount + healthCount.getErrorCount();

        HealthCountSummary healthCountSummary = new HealthCountSummary(successCount, failedCount, qps);
        long totalCount = healthCountSummary.getTotalRequests();
        if (totalCount > 0) {
            healthCountSummary.setErrorPercentage(1.0f * failedCount / totalCount * 100);
        }
        return healthCountSummary;
    }

    public void clear() {
        this.qps = 0;
        this.totalCount = 0;
        this.successCount = 0;
        this.errorCount = 0;
        this.errorPercentage = 0;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public long getTotalRequests() {
        return totalCount;
    }

    public float getErrorPercentage() {
        return errorPercentage;
    }

    public void setErrorPercentage(float errorPercentage) {
        this.errorPercentage = errorPercentage;
    }

    @Override
    public String toString() {
        return "HealthCounts [" + errorCount + " / " + totalCount + " : " + errorPercentage + "%]";
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"totalCount\":");
        builder.append(totalCount);
        builder.append(",");
        builder.append("\"errorCount\":");
        builder.append(errorCount);
        builder.append(",");
        builder.append("\"errorPercentage\":");
        builder.append(errorPercentage);
        builder.append(",");
        builder.append("\"qps\":");
        builder.append(qps);
        builder.append("}");
        return builder.toString();
    }
}
