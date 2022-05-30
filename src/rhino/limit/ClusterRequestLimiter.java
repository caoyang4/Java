package src.rhino.limit;

public class ClusterRequestLimiter extends SingleRequestLimiter {

    public ClusterRequestLimiter(String key, RequestLimiterProperties requestLimiterProperties) {
        super(key, requestLimiterProperties);
    }
}
