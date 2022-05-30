package src.rhino;


/**
 * @author zhanjun on 2017/4/21.
 */
public enum RhinoType {

    CircuitBreaker(1, "circuitbreaker"),
    SingleLimiter(2, "limiter"),
    FaultInject(3, "faultInject"),
    GroupLimiter(4, "limiter"),
    ClusterLimiter(5, "limiter"),
    ThreadPool(6, "threadPool"),
    FeatureLimiter(7, "limiter"),
    Cache(8, "cache"),
    OneLimiter(9, "limiter"),
    CircuitBreaker_Hystrix(10, "circuitbreaker"),
    ThreadPool_Hystrix(11, "threadPool"),
    RhinoSwitch(12, "switch"),
    RetryPolicy(13, "retry"),
    RPCCircuitBreaker(14, "circuitbreaker");

    private int value;
    private String tag;

    RhinoType(int value, String tag) {
        this.value = value;
        this.tag = tag;
    }

    public int getValue() {
        return value;
    }

    public String getTag() {
        return tag;
    }

    public static String get(int value) {
        switch (value) {
            case 1:
                return CircuitBreaker.getTag();
            case 2:
                return SingleLimiter.getTag();
            case 3:
                return FaultInject.getTag();
            case 4:
                return GroupLimiter.getTag();
            case 5:
                return ClusterLimiter.getTag();
            case 6:
                return ThreadPool.getTag();
            case 7:
                return FeatureLimiter.getTag();
            case 8:
                return Cache.getTag();
            case 9:
                return OneLimiter.getTag();
            case 10:
                return CircuitBreaker_Hystrix.getTag();
            case 11:
                return ThreadPool_Hystrix.getTag();
            case 12:
                return RhinoSwitch.getTag();
            case 13:
                return RetryPolicy.getTag();
            case 14:
                return RPCCircuitBreaker.getTag();
            default:
                return null;
        }
    }
    }