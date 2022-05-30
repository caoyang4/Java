package src.rhino.server;

import java.util.Map;

import src.rhino.circuit.CircuitBreaker;
import src.rhino.metric.DefaultRhinoMetric;
import src.rhino.metric.HealthCountBucket;

/**
 * 熔断器接口
 * path: circuit
 * Created by zmz on 2020/12/20.
 */
public class CircuitBreakerRequestHandler implements RhinoHttpRequestHandler {

    @Override
    public Object handleCommand(String command, String rhinoKey, String... params) {
        switch (command) {
            case "profiling":
                return getCircuitProfilingData(rhinoKey);
            case "config":
                return getCircuitConfig(rhinoKey);
            default:
                throw new IllegalArgumentException("Unsurpport command: " + command);
        }
    }

    /**
     * 读取熔断器的统计数据
     * @param rhinoKey
     * @return
     */
    private Object getCircuitProfilingData(String rhinoKey){
        Map<String, HealthCountBucket> circuitMetrix = DefaultRhinoMetric.getHealthCountBuckets();
        if(circuitMetrix == null){
            return null;
        }

        HealthCountBucket bucket = circuitMetrix.get(rhinoKey);
        return bucket == null ? null : bucket.list(60);
    }

    /**
     * 读取熔断器的内存配置
     * @param rhinoKey
     * @return
     */
    private Object getCircuitConfig(String rhinoKey){
        CircuitBreaker circuitBreaker = CircuitBreaker.Factory.getCircuitBreaker(rhinoKey);
        return circuitBreaker == null ? null : circuitBreaker.getCircuitBreakerProperties();
    }
}
