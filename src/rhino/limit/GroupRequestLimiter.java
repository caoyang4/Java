package src.rhino.limit;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanjun on 2017/7/3.
 */
public class GroupRequestLimiter extends AbstractRequestLimiter {

    private String rhinoKey;
    private RequestLimiterProperties properties;
    private ConcurrentHashMap<String, SingleRequestLimiter> requestLimiters = new ConcurrentHashMap<>();

    public GroupRequestLimiter(String rhinoKey, RequestLimiterProperties properties) {
        super(rhinoKey);
        this.rhinoKey = rhinoKey;
        this.properties = properties;
    }

    @Override
    public boolean tryAcquire(String key) {
        if (!properties.getIsActive()) {
            return true;
        }
        SingleRequestLimiter requestLimiter = requestLimiters.get(key);
        if (requestLimiter == null) {
            requestLimiter = new SingleRequestLimiter(rhinoKey + "." + key, properties);
            SingleRequestLimiter requestLimiter0 = requestLimiters.putIfAbsent(key, requestLimiter);
            if (requestLimiter0 != null) {
                requestLimiter = requestLimiter0;
            }
        }
        return requestLimiter.tryAcquire();
    }
}
